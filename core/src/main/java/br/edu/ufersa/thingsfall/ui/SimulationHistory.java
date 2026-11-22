package br.edu.ufersa.thingsfall.ui;

import br.edu.ufersa.thingsfall.common.DynamicArray;
import br.edu.ufersa.thingsfall.common.Stack;
import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.physics.constraint.Constraint;
import br.edu.ufersa.thingsfall.physics.constraint.RevoluteConstraint;

public final class SimulationHistory {
    private final Stack<WorldAction> undoStack = new Stack<>();
    private final Stack<WorldAction> redoStack = new Stack<>();
    private final StartingLayout startingLayout = new StartingLayout();

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void addSquare(PhysicsWorld world, double x, double y, double sideLength, boolean isStatic) {
        execute(world, new CreateObjectAction(ObjectType.SQUARE, x, y, sideLength, isStatic));
    }

    public void addCircle(PhysicsWorld world, double x, double y, double radius, boolean isStatic) {
        execute(world, new CreateObjectAction(ObjectType.CIRCLE, x, y, radius, isStatic));
    }

    public void addRevoluteConstraint(
        PhysicsWorld world,
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        Vector2 worldAnchorOnBodyA,
        Vector2 worldAnchorOnBodyB
    ) {
        int bodyAIndex = world.objects.indexOf(bodyA);
        int bodyBIndex = world.objects.indexOf(bodyB);
        if (bodyAIndex < 0 || bodyBIndex < 0) {
            throw new IllegalArgumentException("Constraint bodies must belong to the physics world");
        }

        execute(
            world,
            new CreateRevoluteConstraintAction(
                bodyAIndex,
                bodyBIndex,
                bodyA.worldToLocal(worldAnchorOnBodyA),
                bodyB.worldToLocal(worldAnchorOnBodyB)
            )
        );
    }

    public void undo(PhysicsWorld world) {
        if (!canUndo()) {
            return;
        }

        WorldAction action = undoStack.pop();
        action.undo(world);
        redoStack.push(action);
    }

    public void redo(PhysicsWorld world) {
        if (!canRedo()) {
            return;
        }

        WorldAction action = redoStack.pop();
        action.apply(world);
        undoStack.push(action);
    }

    public void restore(PhysicsWorld world) {
        startingLayout.restoreLevelObjects(world);

        Stack<WorldAction> actionsToRestore = new Stack<>();
        while (!undoStack.isEmpty()) {
            actionsToRestore.push(undoStack.pop());
        }
        while (!actionsToRestore.isEmpty()) {
            WorldAction action = actionsToRestore.pop();
            action.apply(world);
            undoStack.push(action);
        }
    }

    public void saveStartingLayout() {
        startingLayout.saveLevelObjects();

        Stack<WorldAction> actions = new Stack<>();
        while (!undoStack.isEmpty()) {
            WorldAction action = undoStack.pop();
            if (action instanceof CreateObjectAction createAction) {
                createAction.saveStartingPose();
            }
            actions.push(action);
        }
        while (!actions.isEmpty()) {
            undoStack.push(actions.pop());
        }
    }

    private void execute(PhysicsWorld world, WorldAction action) {
        action.apply(world);
        undoStack.push(action);
        redoStack.clear();
    }

    private interface WorldAction {
        void apply(PhysicsWorld world);

        void undo(PhysicsWorld world);
    }

    private enum ObjectType {
        SQUARE,
        CIRCLE
    }

    private static final class StartingLayout {
        private final DynamicArray<PhysicsObject> levelObjects = new DynamicArray<>();
        private final DynamicArray<ObjectPose> levelObjectPoses = new DynamicArray<>();

        private void restoreLevelObjects(PhysicsWorld world) {
            if (!levelObjectPoses.isEmpty()) {
                if (world.objects.size() != levelObjectPoses.size()) {
                    throw new IllegalStateException("Level objects do not match the saved layout");
                }
                for (int i = 0; i < levelObjectPoses.size(); i++) {
                    levelObjectPoses.get(i).apply(world.objects.get(i));
                }
            }
            levelObjects.clear();
            levelObjects.addAll(world.objects);
        }

        private void saveLevelObjects() {
            levelObjectPoses.clear();
            for (PhysicsObject object : levelObjects) {
                levelObjectPoses.add(new ObjectPose(object));
            }
        }

        private static final class ObjectPose {
            private final double x;
            private final double y;
            private final double rotation;

            private ObjectPose(PhysicsObject object) {
                x = object.position.x;
                y = object.position.y;
                rotation = object.rotation;
            }

            private void apply(PhysicsObject object) {
                object.position = new Vector2(x, y);
                object.rotation = rotation;
            }
        }
    }

    private static final class CreateObjectAction implements WorldAction {
        private final ObjectType type;
        private final double x;
        private final double y;
        private final double size;
        private final boolean isStatic;
        private PhysicsObject object;
        private StartingLayout.ObjectPose startingPose;

        private CreateObjectAction(ObjectType type, double x, double y, double size, boolean isStatic) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
            this.isStatic = isStatic;
        }

        @Override
        public void apply(PhysicsWorld world) {
            object = type == ObjectType.SQUARE
                ? world.addSquare(x, y, size, isStatic)
                : world.addCircle(x, y, size, isStatic);
            if (startingPose != null) {
                startingPose.apply(object);
            }
        }

        private void saveStartingPose() {
            startingPose = new StartingLayout.ObjectPose(object);
        }

        @Override
        public void undo(PhysicsWorld world) {
            PhysicsObject objectToRemove = object;
            world.constraints.removeIf(
                constraint -> constraint.bodyA == objectToRemove || constraint.bodyB == objectToRemove
            );
            world.objects.remove(objectToRemove);
        }
    }

    private static final class CreateRevoluteConstraintAction implements WorldAction {
        private final int bodyAIndex;
        private final int bodyBIndex;
        private final Vector2 localAnchorOnBodyA;
        private final Vector2 localAnchorOnBodyB;
        private Constraint constraint;

        private CreateRevoluteConstraintAction(
            int bodyAIndex,
            int bodyBIndex,
            Vector2 localAnchorOnBodyA,
            Vector2 localAnchorOnBodyB
        ) {
            this.bodyAIndex = bodyAIndex;
            this.bodyBIndex = bodyBIndex;
            this.localAnchorOnBodyA = localAnchorOnBodyA;
            this.localAnchorOnBodyB = localAnchorOnBodyB;
        }

        @Override
        public void apply(PhysicsWorld world) {
            PhysicsObject bodyA = world.objects.get(bodyAIndex);
            PhysicsObject bodyB = world.objects.get(bodyBIndex);
            constraint = new RevoluteConstraint(
                bodyA,
                bodyB,
                bodyA.localToWorld(localAnchorOnBodyA),
                bodyB.localToWorld(localAnchorOnBodyB)
            );
            world.addConstraint(constraint);
        }

        @Override
        public void undo(PhysicsWorld world) {
            world.constraints.remove(constraint);
        }
    }
}
