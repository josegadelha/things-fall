package br.edu.ufersa.thingsfall.physics.constraint;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;

public class ContactConstraint extends Constraint {
    private static final double ALLOWED_LINEAR_PENETRATION = 0.002;
    private static final double MINIMUM_EFFECTIVE_MASS = 0.000001;
    private static final double RESTITUTION_THRESHOLD = 1.0;

    public Object collisionFeatureIdentifier;
    public boolean isReused;
    public double friction;
    public double restitution;
    public double accumulatedNormalImpulse;
    public double accumulatedFrictionImpulse;

    private double inverseMassOfBodyA;
    private double inverseMassOfBodyB;
    private double inverseMomentOfInertiaOfBodyA;
    private double inverseMomentOfInertiaOfBodyB;

    private Vector2 contactNormal;
    private Vector2 contactTangent;
    private Vector2 localContactPointOnBodyA;
    private Vector2 localContactPointOnBodyB;
    private Vector2 worldContactPointOnBodyA;
    private Vector2 worldContactPointOnBodyB;
    private Vector2 contactOffsetFromBodyA;
    private Vector2 contactOffsetFromBodyB;

    private double penetration;
    private double initialRelativeNormalVelocity;

    public ContactConstraint(
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        Vector2 contactPoint,
        Vector2 contactNormal,
        double penetration,
        Object collisionFeatureIdentifier
    ) {
        super(bodyA, bodyB);

        if (contactPoint == null || contactNormal == null) {
            throw new IllegalArgumentException("The contact point and normal cannot be null.");
        }
        if (!Double.isFinite(penetration) || penetration < 0.0) {
            throw new IllegalArgumentException("Penetration must be finite and non-negative.");
        }

        this.collisionFeatureIdentifier = collisionFeatureIdentifier;
        isReused = false;
        friction = Math.sqrt(bodyA.friction * bodyB.friction);
        restitution = Math.sqrt(bodyA.restitution * bodyB.restitution);
        accumulatedNormalImpulse = 0.0;
        accumulatedFrictionImpulse = 0.0;

        updateInverseMassesAndInertias();
        setCollisionData(contactPoint, contactNormal, penetration);

        // Save the incoming normal velocity before any contact impulses are applied.
        Vector2 velocityAtContactOnBodyA = getVelocityAtContact(bodyA, contactOffsetFromBodyA);
        Vector2 velocityAtContactOnBodyB = getVelocityAtContact(bodyB, contactOffsetFromBodyB);
        Vector2 relativeVelocity = velocityAtContactOnBodyB.subtract(velocityAtContactOnBodyA);
        initialRelativeNormalVelocity = this.contactNormal.dotProduct(relativeVelocity);
    }

    public void setCollisionData(
        Vector2 contactPoint,
        Vector2 contactNormal,
        double penetration
    ) {
        if (contactPoint == null || contactNormal == null) {
            throw new IllegalArgumentException("The contact point and normal cannot be null.");
        }
        if (!Double.isFinite(contactNormal.x)
            || !Double.isFinite(contactNormal.y)
            || (contactNormal.x == 0.0 && contactNormal.y == 0.0)) {
            throw new IllegalArgumentException("The contact normal must be finite and non-zero.");
        }
        if (!Double.isFinite(penetration) || penetration < 0.0) {
            throw new IllegalArgumentException("Penetration must be finite and non-negative.");
        }

        this.contactNormal = contactNormal;
        this.penetration = penetration;

        // Keep one contact point fixed relative to each body as they move.
        localContactPointOnBodyA = bodyA.worldToLocal(contactPoint);
        localContactPointOnBodyB = bodyB.worldToLocal(contactPoint);
        worldContactPointOnBodyA = bodyA.localToWorld(localContactPointOnBodyA);
        worldContactPointOnBodyB = bodyB.localToWorld(localContactPointOnBodyB);
        contactOffsetFromBodyA = worldContactPointOnBodyA.subtract(bodyA.position);
        contactOffsetFromBodyB = worldContactPointOnBodyB.subtract(bodyB.position);
        contactTangent = this.contactNormal.rotate90CW();
    }

    @Override
    public void update(ConstraintSettings worldSettings) {
        worldContactPointOnBodyA = bodyA.localToWorld(localContactPointOnBodyA);
        worldContactPointOnBodyB = bodyB.localToWorld(localContactPointOnBodyB);
        contactOffsetFromBodyA = worldContactPointOnBodyA.subtract(bodyA.position);
        contactOffsetFromBodyB = worldContactPointOnBodyB.subtract(bodyB.position);
        updateInverseMassesAndInertias();

        // Restitution uses the velocity before warm starting changes the bodies.
        Vector2 velocityAtContactOnBodyA = getVelocityAtContact(bodyA, contactOffsetFromBodyA);
        Vector2 velocityAtContactOnBodyB = getVelocityAtContact(bodyB, contactOffsetFromBodyB);
        initialRelativeNormalVelocity = contactNormal.dotProduct(
            velocityAtContactOnBodyB.subtract(velocityAtContactOnBodyA)
        );

        // Reapply the impulses accumulated during the previous simulation step.
        Vector2 normalImpulse = contactNormal.scale(accumulatedNormalImpulse);
        Vector2 frictionImpulse = contactTangent.scale(accumulatedFrictionImpulse);
        Vector2 totalImpulse = normalImpulse.add(frictionImpulse);

        bodyA.velocity = bodyA.velocity.subtract(totalImpulse.scale(inverseMassOfBodyA));
        bodyA.angularVelocity -= inverseMomentOfInertiaOfBodyA * contactOffsetFromBodyA.crossProduct(totalImpulse);
        bodyB.velocity = bodyB.velocity.add(totalImpulse.scale(inverseMassOfBodyB));
        bodyB.angularVelocity += inverseMomentOfInertiaOfBodyB * contactOffsetFromBodyB.crossProduct(totalImpulse);
    }

    @Override
    public void solve(double timeStep, ConstraintSettings worldSettings) {
        ConstraintSettings settings = constraintSettings != null ? constraintSettings : worldSettings;
        solveContact(timeStep, settings);
        solveFriction();
    }

    // Resolves motion into the contact normal and accumulates a nonnegative impulse.
    private void solveContact(double timeStep, ConstraintSettings settings) {
        Vector2 velocityAtContactOnBodyA = getVelocityAtContact(bodyA, contactOffsetFromBodyA);
        Vector2 velocityAtContactOnBodyB = getVelocityAtContact(bodyB, contactOffsetFromBodyB);
        Vector2 relativeVelocity = velocityAtContactOnBodyB.subtract(velocityAtContactOnBodyA);
        double relativeNormalVelocity = contactNormal.dotProduct(relativeVelocity);

        double normalOffsetForBodyA = contactOffsetFromBodyA.crossProduct(contactNormal);
        double normalOffsetForBodyB = contactOffsetFromBodyB.crossProduct(contactNormal);
        double effectiveMass =
            inverseMassOfBodyA + inverseMassOfBodyB
                + normalOffsetForBodyA * normalOffsetForBodyA
                * inverseMomentOfInertiaOfBodyA
                + normalOffsetForBodyB * normalOffsetForBodyB
                * inverseMomentOfInertiaOfBodyB;
        if (effectiveMass < MINIMUM_EFFECTIVE_MASS) return;

        double velocityBias = 0.0;
        double effectiveMassScale = 1.0;
        double accumulatedImpulseScale = 0.0;

        if ("baumgarte".equals(settings.mode)) {
            double separation = Math.min(0.0, -penetration + ALLOWED_LINEAR_PENETRATION);
            velocityBias = (settings.baumgarteFactor / timeStep) * separation;
        } else if ("soft".equals(settings.mode)) {
            double maximumHertz = 0.25 / timeStep;
            double hertz = Math.min(settings.contactSoftConstraint.hertz, maximumHertz);
            SoftConstraintParameters softParameters =
                SoftConstraintParameters.getSoftConstraintParameters(
                    hertz,
                    settings.contactSoftConstraint.dampingRatio,
                    timeStep
                );
            double separation = Math.min(0.0, -penetration + ALLOWED_LINEAR_PENETRATION);
            velocityBias = Math.max(softParameters.biasRate * separation, -settings.contactSpeed);
            effectiveMassScale = softParameters.massScale;
            accumulatedImpulseScale = softParameters.impulseScale;
        }

        // Solve for the normal impulse, including the softness of the contact.
        double normalImpulseMagnitude = -(effectiveMassScale * relativeNormalVelocity + velocityBias) / effectiveMass;
        normalImpulseMagnitude += -accumulatedImpulseScale * accumulatedNormalImpulse / effectiveMass;

        // Contacts cannot pull bodies together, so clamp the accumulated
        // normal impulse to zero or greater and apply only its change.
        double previousAccumulatedNormalImpulse = accumulatedNormalImpulse;
        accumulatedNormalImpulse = Math.max(previousAccumulatedNormalImpulse + normalImpulseMagnitude, 0.0);
        normalImpulseMagnitude = accumulatedNormalImpulse - previousAccumulatedNormalImpulse;
        if (normalImpulseMagnitude == 0.0) return;

        Vector2 normalImpulse = contactNormal.scale(normalImpulseMagnitude);
        bodyA.applyImpulse(normalImpulse.scale(-1.0), worldContactPointOnBodyA);
        bodyB.applyImpulse(normalImpulse, worldContactPointOnBodyB);
    }

    private void solveFriction() {
        if (friction <= 0.0) return;

        Vector2 velocityAtContactOnBodyA = getVelocityAtContact(bodyA, contactOffsetFromBodyA);
        Vector2 velocityAtContactOnBodyB = getVelocityAtContact(bodyB, contactOffsetFromBodyB);
        Vector2 relativeVelocity = velocityAtContactOnBodyB.subtract(velocityAtContactOnBodyA);
        double relativeTangentVelocity = contactTangent.dotProduct(relativeVelocity);

        double tangentOffsetForBodyA = contactOffsetFromBodyA.crossProduct(contactTangent);
        double tangentOffsetForBodyB = contactOffsetFromBodyB.crossProduct(contactTangent);
        double effectiveTangentMass =
            inverseMassOfBodyA + inverseMassOfBodyB
                + tangentOffsetForBodyA * tangentOffsetForBodyA
                * inverseMomentOfInertiaOfBodyA
                + tangentOffsetForBodyB * tangentOffsetForBodyB
                * inverseMomentOfInertiaOfBodyB;
        if (effectiveTangentMass < MINIMUM_EFFECTIVE_MASS) return;

        double frictionImpulseMagnitude = -relativeTangentVelocity / effectiveTangentMass;
        double maximumFrictionImpulse = friction * accumulatedNormalImpulse;
        double previousAccumulatedFrictionImpulse = accumulatedFrictionImpulse;

        // Coulomb friction bounds the tangent impulse by friction times the accumulated normal impulse.
        accumulatedFrictionImpulse = Math.clamp(
            previousAccumulatedFrictionImpulse + frictionImpulseMagnitude,
            -maximumFrictionImpulse,
            maximumFrictionImpulse
        );
        frictionImpulseMagnitude = accumulatedFrictionImpulse - previousAccumulatedFrictionImpulse;

        Vector2 frictionImpulse = contactTangent.scale(frictionImpulseMagnitude);
        bodyA.applyImpulse(frictionImpulse.scale(-1.0), worldContactPointOnBodyA);
        bodyB.applyImpulse(frictionImpulse, worldContactPointOnBodyB);
    }

    public void applyRestitution() {
        if (restitution == 0.0 || isReused) return;
        if (initialRelativeNormalVelocity > -RESTITUTION_THRESHOLD) return;

        double normalOffsetForBodyA = contactOffsetFromBodyA.crossProduct(contactNormal);
        double normalOffsetForBodyB = contactOffsetFromBodyB.crossProduct(contactNormal);
        double effectiveMass =
            inverseMassOfBodyA + inverseMassOfBodyB
                + normalOffsetForBodyA * normalOffsetForBodyA
                * inverseMomentOfInertiaOfBodyA
                + normalOffsetForBodyB * normalOffsetForBodyB
                * inverseMomentOfInertiaOfBodyB;
        if (effectiveMass < MINIMUM_EFFECTIVE_MASS) return;

        Vector2 velocityAtContactOnBodyA = getVelocityAtContact(bodyA, contactOffsetFromBodyA);
        Vector2 velocityAtContactOnBodyB = getVelocityAtContact(bodyB, contactOffsetFromBodyB);
        double currentRelativeNormalVelocity = contactNormal.dotProduct(
            velocityAtContactOnBodyB.subtract(velocityAtContactOnBodyA)
        );

        // Restore the separating speed implied by the restitution coefficient.
        double restitutionImpulseMagnitude = -(
            currentRelativeNormalVelocity + restitution * initialRelativeNormalVelocity
        ) / effectiveMass;
        if (restitutionImpulseMagnitude > 0.0) {
            Vector2 restitutionImpulse = contactNormal.scale(restitutionImpulseMagnitude);
            bodyA.applyImpulse(restitutionImpulse.scale(-1.0), worldContactPointOnBodyA);
            bodyB.applyImpulse(restitutionImpulse, worldContactPointOnBodyB);
        }
    }

    private Vector2 getVelocityAtContact(PhysicsObject body, Vector2 contactOffset) {
        Vector2 angularVelocityAtContact = contactOffset.crossProductScalarVector(body.angularVelocity);
        return body.velocity.add(angularVelocityAtContact);
    }

    private void updateInverseMassesAndInertias() {
        inverseMassOfBodyA = bodyA.isStatic ? 0.0 : 1.0 / bodyA.mass;
        inverseMassOfBodyB = bodyB.isStatic ? 0.0 : 1.0 / bodyB.mass;
        inverseMomentOfInertiaOfBodyA = bodyA.isStatic ? 0.0 : 1.0 / bodyA.momentOfInertia;
        inverseMomentOfInertiaOfBodyB = bodyB.isStatic ? 0.0 : 1.0 / bodyB.momentOfInertia;
    }
}
