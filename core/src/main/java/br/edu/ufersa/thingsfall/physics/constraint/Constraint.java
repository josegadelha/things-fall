package br.edu.ufersa.thingsfall.physics.constraint;

import br.edu.ufersa.thingsfall.physics.PhysicsObject;

public abstract class Constraint {
    public PhysicsObject bodyA;
    public PhysicsObject bodyB;
    public ConstraintSettings constraintSettings;

    public Constraint(PhysicsObject bodyA, PhysicsObject bodyB) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
    }

    protected ConstraintSettings resolveSettings(ConstraintSettings worldSettings) {
        ConstraintSettings resolvedSettings = constraintSettings != null ? constraintSettings : worldSettings;

        if (resolvedSettings == null) {
            throw new IllegalArgumentException("Constraint settings cannot be null.");
        }

        return resolvedSettings;
    }

    public void update(ConstraintSettings constraintSettings) {}

    public void solve(double timeStep, ConstraintSettings constraintSettings) {}
}
