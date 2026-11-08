package br.edu.ufersa.thingsfall.physics.constraint;

import br.edu.ufersa.thingsfall.math.Matrix2x2;
import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;

public class RevoluteConstraint extends Constraint {
    // Each fixed local anchor identifies the joint point relative to its body.
    // Converting these points back to world coordinates keeps the joint attached
    // while the bodies translate and rotate.
    public Vector2 localAnchorOnBodyA;
    public Vector2 localAnchorOnBodyB;

    // Current world-space anchor points and their offsets from each body's center.
    public Vector2 worldAnchorOnBodyA = new Vector2(0.0, 0.0);
    public Vector2 worldAnchorOnBodyB = new Vector2(0.0, 0.0);
    public Vector2 anchorOffsetFromBodyA = new Vector2(0.0, 0.0);
    public Vector2 anchorOffsetFromBodyB = new Vector2(0.0, 0.0);

    // Effective mass matrix maps an anchor impulse to a change in relative velocity.
    public double[][] effectiveMassMatrix = {{0.0, 0.0}, {0.0, 0.0}};

    public double inverseMassOfBodyA;
    public double inverseMassOfBodyB;
    public double inverseMomentOfInertiaOfBodyA;
    public double inverseMomentOfInertiaOfBodyB;

    public Double lowerAngleLimit;
    public Double upperAngleLimit;
    public double angleLimitViolation;
    public double currentAngle;
    public double stiffness;

    // Retained between steps for warm starting the anchor constraint.
    public Vector2 accumulatedAnchorImpulse = new Vector2(0.0, 0.0);

    public boolean motorEnabled;
    public Double motorTargetAngle;
    public double motorFrequency = 6.0;
    public double motorDampingRatio = 1.0;
    public double motorSpeed;
    public double maxMotorForce = 5.0;

    // Creates a joint with a separate world-space anchor on each body.
    // This overload is used when the user selects two points independently.
    public RevoluteConstraint(
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        Vector2 worldAnchorOnBodyA,
        Vector2 worldAnchorOnBodyB
    ) {
        super(bodyA, bodyB);
        requireAnchor(worldAnchorOnBodyA, "The first revolute anchor");
        requireAnchor(worldAnchorOnBodyB, "The second revolute anchor");

        localAnchorOnBodyA = bodyA.worldToLocal(worldAnchorOnBodyA);
        localAnchorOnBodyB = bodyB.worldToLocal(worldAnchorOnBodyB);
        lowerAngleLimit = null;
        upperAngleLimit = null;
        stiffness = 1.0;
        updateInverseMassesAndInertias();
    }

    // Creates a joint whose two bodies share one world-space anchor initially.
    public RevoluteConstraint(
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        Vector2 worldAnchor,
        Double lowerAngleLimit,
        Double upperAngleLimit,
        double stiffness
    ) {
        super(bodyA, bodyB);
        requireAnchor(worldAnchor, "The revolute anchor");

        localAnchorOnBodyA = bodyA.worldToLocal(worldAnchor);
        localAnchorOnBodyB = bodyB.worldToLocal(worldAnchor);
        this.lowerAngleLimit = lowerAngleLimit;
        this.upperAngleLimit = upperAngleLimit;
        this.stiffness = stiffness;
        updateInverseMassesAndInertias();
    }

    @Override
    public void update(ConstraintSettings worldSettings) {
        // Recreate the world-space anchor positions from their fixed body-local points.
        worldAnchorOnBodyA = bodyA.localToWorld(localAnchorOnBodyA);
        worldAnchorOnBodyB = bodyB.localToWorld(localAnchorOnBodyB);
        anchorOffsetFromBodyA = worldAnchorOnBodyA.subtract(bodyA.position);
        anchorOffsetFromBodyB = worldAnchorOnBodyB.subtract(bodyB.position);

        currentAngle = bodyB.rotation - bodyA.rotation;
        updateInverseMassesAndInertias();

        ConstraintSettings settings = resolveSettings(worldSettings);

        // Apply last step's anchor impulse before this step's iterative solve.
        if (settings.warmStarting) {
            bodyA.applyImpulse(accumulatedAnchorImpulse.scale(-1.0), worldAnchorOnBodyA);
            bodyB.applyImpulse(accumulatedAnchorImpulse, worldAnchorOnBodyB);
        }

        // A nonzero violation indicates that a configured angular limit was crossed.
        if (lowerAngleLimit != null && currentAngle < lowerAngleLimit) {
            angleLimitViolation = currentAngle - lowerAngleLimit;
        } else if (upperAngleLimit != null && currentAngle > upperAngleLimit) {
            angleLimitViolation = currentAngle - upperAngleLimit;
        } else {
            angleLimitViolation = 0.0;
        }
    }

    @Override
    public void solve(double timeStep, ConstraintSettings worldSettings) {
        requirePositiveTimeStep(timeStep);
        ConstraintSettings settings = resolveSettings(worldSettings);

        solveAnchorConstraint(timeStep, settings);
        solveMotor(timeStep);
        solveAngleLimits(timeStep, settings);
    }

    // Applies an impulse that brings both world-space anchors together.
    public void solveAnchorConstraint(double timeStep, ConstraintSettings settings) {
        requirePositiveTimeStep(timeStep);

        double inverseMassSum = inverseMassOfBodyA + inverseMassOfBodyB;

        effectiveMassMatrix[0][0] =
            inverseMassSum
                + anchorOffsetFromBodyA.y * anchorOffsetFromBodyA.y
                    * inverseMomentOfInertiaOfBodyA
                + anchorOffsetFromBodyB.y * anchorOffsetFromBodyB.y
                    * inverseMomentOfInertiaOfBodyB;
        effectiveMassMatrix[0][1] =
            -anchorOffsetFromBodyA.y * anchorOffsetFromBodyA.x
                * inverseMomentOfInertiaOfBodyA
                - anchorOffsetFromBodyB.y * anchorOffsetFromBodyB.x
                    * inverseMomentOfInertiaOfBodyB;
        effectiveMassMatrix[1][0] = effectiveMassMatrix[0][1];
        effectiveMassMatrix[1][1] =
            inverseMassSum
                + anchorOffsetFromBodyA.x * anchorOffsetFromBodyA.x
                    * inverseMomentOfInertiaOfBodyA
                + anchorOffsetFromBodyB.x * anchorOffsetFromBodyB.x
                    * inverseMomentOfInertiaOfBodyB;

        Vector2 anchorVelocityOnBodyA = getVelocityAtAnchor(bodyA, anchorOffsetFromBodyA);
        Vector2 anchorVelocityOnBodyB = getVelocityAtAnchor(bodyB, anchorOffsetFromBodyB);
        Vector2 relativeAnchorVelocity = anchorVelocityOnBodyB.subtract(anchorVelocityOnBodyA);
        Vector2 anchorPositionError = worldAnchorOnBodyB.subtract(worldAnchorOnBodyA);

        Vector2 correctionVelocity = new Vector2(0.0, 0.0);
        double effectiveMassScale = 1.0;
        double previousImpulseScale = 0.0;

        if ("baumgarte".equals(settings.mode)) {
            correctionVelocity = anchorPositionError.scale(settings.baumgarteFactor / timeStep);
        } else if ("soft".equals(settings.mode)) {
            double maximumHertz = 0.25 / timeStep;
            double hertz = Math.min(settings.jointSoftConstraint.hertz, maximumHertz);
            SoftConstraintParameters softParameters =
                SoftConstraintParameters.getSoftConstraintParameters(
                    hertz,
                    settings.jointSoftConstraint.dampingRatio,
                    timeStep
                );
            correctionVelocity = anchorPositionError.scale(softParameters.biasRate);
            effectiveMassScale = softParameters.massScale;
            previousImpulseScale = softParameters.impulseScale;
        }

        Vector2 anchorImpulse = Matrix2x2.solve(
            effectiveMassMatrix,
            relativeAnchorVelocity.add(correctionVelocity).scale(-1.0)
        );

        if ("soft".equals(settings.mode)) {
            anchorImpulse = new Vector2(
                effectiveMassScale * anchorImpulse.x - previousImpulseScale * accumulatedAnchorImpulse.x,
                effectiveMassScale * anchorImpulse.y - previousImpulseScale * accumulatedAnchorImpulse.y
            );
        }

        // The two bodies receive equal and opposite impulses at their anchors.
        bodyA.applyImpulse(anchorImpulse.scale(-1.0), worldAnchorOnBodyA);
        bodyB.applyImpulse(anchorImpulse, worldAnchorOnBodyB);

        if (settings.warmStarting) {
            accumulatedAnchorImpulse = accumulatedAnchorImpulse.add(anchorImpulse);
        }
    }

    // Drives relative angular velocity, or springs toward a target angle.
    public void solveMotor(double timeStep) {
        requirePositiveTimeStep(timeStep);

        if (!motorEnabled) return;

        double inverseMomentOfInertiaSum = inverseMomentOfInertiaOfBodyA + inverseMomentOfInertiaOfBodyB;
        if (inverseMomentOfInertiaSum == 0.0) return;

        double relativeAngularVelocity = bodyB.angularVelocity - bodyA.angularVelocity;
        double motorImpulse;

        if (motorTargetAngle != null) {
            double effectiveMomentOfInertia = 1.0 / inverseMomentOfInertiaSum;
            double angularFrequency = 2.0 * Math.PI * motorFrequency;
            double springStiffness = effectiveMomentOfInertia * angularFrequency * angularFrequency;
            double dampingCoefficient = 2.0 * effectiveMomentOfInertia * motorDampingRatio * angularFrequency;
            double softness = 1.0 / (timeStep * (dampingCoefficient + springStiffness * timeStep));
            double targetAngleVelocity = (currentAngle - motorTargetAngle) * springStiffness * timeStep * softness;

            motorImpulse = -(relativeAngularVelocity + targetAngleVelocity) / (inverseMomentOfInertiaSum + softness);
        } else {
            motorImpulse = (motorSpeed - relativeAngularVelocity) / inverseMomentOfInertiaSum;
        }

        // A motor's maximum force becomes a maximum impulse over this time step.
        double maximumMotorImpulse = maxMotorForce * timeStep;
        motorImpulse = Math.clamp(motorImpulse, -maximumMotorImpulse, maximumMotorImpulse);

        bodyA.angularVelocity -= inverseMomentOfInertiaOfBodyA * motorImpulse;
        bodyB.angularVelocity += inverseMomentOfInertiaOfBodyB * motorImpulse;
    }

    // Corrects an angle-limit violation without pushing farther past the limit.
    public void solveAngleLimits(double timeStep, ConstraintSettings settings) {
        requirePositiveTimeStep(timeStep);

        if ((lowerAngleLimit == null && upperAngleLimit == null) || angleLimitViolation == 0.0) return;

        double inverseMomentOfInertiaSum = inverseMomentOfInertiaOfBodyA + inverseMomentOfInertiaOfBodyB;
        if (inverseMomentOfInertiaSum < 1e-6) return;

        double relativeAngularVelocity = bodyB.angularVelocity - bodyA.angularVelocity;
        double correctionVelocity = 0.0;
        double effectiveMassScale = 1.0;

        if ("baumgarte".equals(settings.mode)) {
            correctionVelocity = (settings.baumgarteFactor / timeStep) * angleLimitViolation;
        } else if ("soft".equals(settings.mode)) {
            double maximumHertz = 0.25 / timeStep;
            double hertz = Math.min(settings.jointSoftConstraint.hertz, maximumHertz);
            SoftConstraintParameters softParameters =
                SoftConstraintParameters.getSoftConstraintParameters(
                    hertz,
                    settings.jointSoftConstraint.dampingRatio,
                    timeStep
                );
            correctionVelocity = softParameters.biasRate * angleLimitViolation;
            effectiveMassScale = softParameters.massScale;
        }

        double angularImpulse = -(effectiveMassScale * (relativeAngularVelocity + correctionVelocity))
            / inverseMomentOfInertiaSum;

        // Positive angle error means the upper limit was crossed; negative
        // error means the lower limit was crossed.
        if (angleLimitViolation > 0.0) {
            angularImpulse = Math.min(angularImpulse, 0.0);
        } else {
            angularImpulse = Math.max(angularImpulse, 0.0);
        }

        bodyA.angularVelocity -= inverseMomentOfInertiaOfBodyA * angularImpulse;
        bodyB.angularVelocity += inverseMomentOfInertiaOfBodyB * angularImpulse;
    }

    // Enables the motor and optionally changes its target and tuning.
    // A null target selects constant-speed mode.
    public void setMotorTargetAngle(
        Double targetAngle,
        Double maximumForce,
        Double frequency,
        Double dampingRatio,
        boolean targetToWrappedClosest
    ) {
        if (targetAngle != null && targetToWrappedClosest) {
            double shortestAngleDifference = Math.atan2(
                Math.sin(targetAngle - currentAngle),
                Math.cos(targetAngle - currentAngle)
            );
            targetAngle = currentAngle + shortestAngleDifference;

            if (lowerAngleLimit != null
                && upperAngleLimit != null
                && (targetAngle < lowerAngleLimit || targetAngle > upperAngleLimit)) {
                double distanceToLowerLimit = Math.abs(angleDifferenceInRadians(lowerAngleLimit, targetAngle));
                double distanceToUpperLimit = Math.abs(angleDifferenceInRadians(upperAngleLimit, targetAngle));
                targetAngle = distanceToLowerLimit < distanceToUpperLimit ? lowerAngleLimit : upperAngleLimit;
            }

            double minimumAllowedAngle = lowerAngleLimit != null ? lowerAngleLimit : Double.NEGATIVE_INFINITY;
            double maximumAllowedAngle = upperAngleLimit != null ? upperAngleLimit : Double.POSITIVE_INFINITY;
            targetAngle = Math.clamp(targetAngle, minimumAllowedAngle, maximumAllowedAngle);
        }

        motorEnabled = true;
        motorTargetAngle = targetAngle;
        if (maximumForce != null) {
            maxMotorForce = maximumForce;
        }
        if (frequency != null) {
            motorFrequency = frequency;
        }
        if (dampingRatio != null) {
            motorDampingRatio = dampingRatio;
        }
    }

    public double getRotation() {
        return bodyB.rotation - bodyA.rotation;
    }

    private Vector2 getVelocityAtAnchor(PhysicsObject body, Vector2 anchorOffset) {
        Vector2 angularVelocityAtAnchor = anchorOffset.crossProductScalarVector(body.angularVelocity);
        return body.velocity.add(angularVelocityAtAnchor);
    }

    private double angleDifferenceInRadians(double firstAngle, double secondAngle) {
        return ((firstAngle - secondAngle + Math.PI)
            % (2.0 * Math.PI) + 2.0 * Math.PI)
            % (2.0 * Math.PI) - Math.PI;
    }

    private void updateInverseMassesAndInertias() {
        inverseMassOfBodyA = bodyA.isStatic ? 0.0 : 1.0 / bodyA.mass;
        inverseMassOfBodyB = bodyB.isStatic ? 0.0 : 1.0 / bodyB.mass;
        inverseMomentOfInertiaOfBodyA = bodyA.isStatic ? 0.0 : 1.0 / bodyA.momentOfInertia;
        inverseMomentOfInertiaOfBodyB = bodyB.isStatic ? 0.0 : 1.0 / bodyB.momentOfInertia;
    }

    private void requireAnchor(Vector2 anchor, String description) {
        if (anchor == null) {
            throw new IllegalArgumentException(description + " cannot be null.");
        }
    }

    private void requirePositiveTimeStep(double timeStep) {
        if (!Double.isFinite(timeStep) || timeStep <= 0.0) {
            throw new IllegalArgumentException("The time step must be finite and positive.");
        }
    }
}
