package br.edu.ufersa.thingsfall.ui;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.physics.constraint.Constraint;
import br.edu.ufersa.thingsfall.physics.constraint.RevoluteConstraint;

import java.util.ArrayList;
import java.util.List;

public final class SimulationHistory {
    private final List<WorldAction> undoStack = new ArrayList<>();
    private final List<WorldAction> redoStack = new ArrayList<>();

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
                copy(worldAnchorOnBodyA),
                copy(worldAnchorOnBodyB)
            )
        );
    }

    public void undo(PhysicsWorld world) {
        if (!canUndo()) {
            return;
        }

        WorldAction action = undoStack.remove(undoStack.size() - 1);
        action.undo(world);
        redoStack.add(action);
    }

    public void redo(PhysicsWorld world) {
        if (!canRedo()) {
            return;
        }

        WorldAction action = redoStack.remove(redoStack.size() - 1);
        action.apply(world);
        undoStack.add(action);
    }

    public void restore(PhysicsWorld world) {
        for (WorldAction action : undoStack) {
            action.apply(world);
        }
    }

    private void execute(PhysicsWorld world, WorldAction action) {
        action.apply(world);
        undoStack.add(action);
        redoStack.clear();
    }

    private static Vector2 copy(Vector2 vector) {
        return new Vector2(vector.x, vector.y);
    }

    private interface WorldAction {
        void apply(PhysicsWorld world);

        void undo(PhysicsWorld world);
    }

    private enum ObjectType {
        SQUARE,
        CIRCLE
    }

    private static final class CreateObjectAction implements WorldAction {
        private final ObjectType type;
        private final double x;
        private final double y;
        private final double size;
        private final boolean isStatic;
        private PhysicsObject object;

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
        private final Vector2 worldAnchorOnBodyA;
        private final Vector2 worldAnchorOnBodyB;
        private Constraint constraint;

        private CreateRevoluteConstraintAction(
            int bodyAIndex,
            int bodyBIndex,
            Vector2 worldAnchorOnBodyA,
            Vector2 worldAnchorOnBodyB
        ) {
            this.bodyAIndex = bodyAIndex;
            this.bodyBIndex = bodyBIndex;
            this.worldAnchorOnBodyA = worldAnchorOnBodyA;
            this.worldAnchorOnBodyB = worldAnchorOnBodyB;
        }

        @Override
        public void apply(PhysicsWorld world) {
            PhysicsObject bodyA = world.objects.get(bodyAIndex);
            PhysicsObject bodyB = world.objects.get(bodyBIndex);
            constraint = new RevoluteConstraint(
                bodyA,
                bodyB,
                worldAnchorOnBodyA,
                worldAnchorOnBodyB
            );
            world.addConstraint(constraint);
        }

        @Override
        public void undo(PhysicsWorld world) {
            world.constraints.remove(constraint);
        }
    }
}
