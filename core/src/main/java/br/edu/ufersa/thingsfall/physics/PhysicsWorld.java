package br.edu.ufersa.thingsfall.physics;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.collision.CollisionHelper;
import br.edu.ufersa.thingsfall.physics.constraint.Constraint;
import br.edu.ufersa.thingsfall.physics.constraint.ContactConstraint;
import br.edu.ufersa.thingsfall.physics.constraint.ConstraintSettings;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {
    public static final double MAX_OBJECT_DISTANCE = 1000.0;
    private static final double VELOCITY_DAMPING_RATE = 2.0;
    private static final double VELOCITY_STOP_THRESHOLD = 0.0001;

    public List<PhysicsObject> objects;
    public List<Constraint> constraints;
    public Vector2 gravity;
    public int constraintIterations;
    public ConstraintSettings constraintSettings;
    public boolean reduceVelocityEnabled;
    public boolean collisionsEnabled;

    public PhysicsWorld() {
        this.objects = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.gravity = new Vector2(0, -9.81);
        this.constraintIterations = 10;
        this.constraintSettings = new ConstraintSettings();
        this.collisionsEnabled = true;
    }

    public void addSquare(double x, double y, double sideLength, boolean isStatic) {
        if (!Double.isFinite(sideLength) || sideLength <= 0) {
            throw new IllegalArgumentException("Square side length must be finite and positive");
        }

        double halfSideLength = sideLength / 2;
        double mass = sideLength * sideLength;
        double momentOfInertia = mass * sideLength * sideLength / 6;
        PhysicsObject square = new PhysicsObject(
            x,
            y,
            isStatic,
            mass,
            momentOfInertia,
            new ConvexPolygonShape(
                new Vector2(-halfSideLength, -halfSideLength),
                new Vector2(halfSideLength, -halfSideLength),
                new Vector2(halfSideLength, halfSideLength),
                new Vector2(-halfSideLength, halfSideLength)
            )
        );
        objects.add(square);
    }

    public void addCircle(double x, double y, double radius, boolean isStatic) {
        if (!Double.isFinite(radius) || radius <= 0) {
            throw new IllegalArgumentException("Circle radius must be finite and positive");
        }

        double mass = Math.PI * radius * radius;
        double momentOfInertia = mass * radius * radius / 2;
        PhysicsObject circle = new PhysicsObject(
            x,
            y,
            isStatic,
            mass,
            momentOfInertia,
            new CircleShape(radius)
        );
        objects.add(circle);
    }

    public void addGeometry(PhysicsObject... objects) {
        for (PhysicsObject object : objects) {
            if (object == null) {
                throw new IllegalArgumentException("Geometry object cannot be null.");
            }
            this.objects.add(object);
        }
    }

    public void addConstraint(Constraint constraint) {
        if (constraint == null) {
            throw new IllegalArgumentException("Constraint cannot be null.");
        }
        constraints.add(constraint);
    }

    public void solveConstraints(double timeStep, int iterationCount) {
        if (constraints.isEmpty() || iterationCount <= 0) {
            return;
        }

        for (Constraint constraint : constraints) {
            constraint.update(constraintSettings);
        }

        for (int iteration = 0; iteration < iterationCount; iteration++) {
            for (Constraint constraint : constraints) {
                constraint.solve(timeStep, constraintSettings);
            }
        }

        for (Constraint constraint : constraints) {
            if (constraint instanceof ContactConstraint) {
                ((ContactConstraint) constraint).applyRestitution();
            }
        }
    }

    public void step(double dt) {
        // Apply gravity only to dynamic objects.
        for (PhysicsObject object : objects) {
            if (!object.isStatic) {
                object.velocity = object.velocity.add(gravity.scale(dt));
            }
        }

        if (collisionsEnabled) detectCollisions();
        solveConstraints(dt, constraintIterations);

        // Apply velocity damping to reduce the velocity of dynamic objects over time.
        if (reduceVelocityEnabled) {
            double dampingFactor = Math.exp(-VELOCITY_DAMPING_RATE * dt);
            for (PhysicsObject object : objects) {
                if (object.isStatic) {
                    continue;
                }

                object.velocity = object.velocity.scale(dampingFactor);
                if (object.velocity.length() < VELOCITY_STOP_THRESHOLD) {
                    object.velocity = new Vector2(0, 0);
                }

                object.angularVelocity *= dampingFactor;
                if (Math.abs(object.angularVelocity) < VELOCITY_STOP_THRESHOLD) {
                    object.angularVelocity = 0;
                }
            }
        }

        // Update all objects.
        for (PhysicsObject object : objects) {
            object.step(dt);
        }

        // Remove objects that are too far away.
        objects.removeIf(object -> Math.hypot(object.position.x, object.position.y) > MAX_OBJECT_DISTANCE);
    }

    private void detectCollisions() {
        List<ContactConstraint> contactConstraintsForReuse = new ArrayList<>();
        for (Constraint constraint : constraints) {
            if (constraint instanceof ContactConstraint) {
                contactConstraintsForReuse.add((ContactConstraint) constraint);
            }
        }

        constraints.removeIf(constraint -> constraint instanceof ContactConstraint);
        constraints.addAll(CollisionHelper.handleCollisions(objects, contactConstraintsForReuse, constraints));
    }
}
