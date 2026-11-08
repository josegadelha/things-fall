package br.edu.ufersa.thingsfall.physics.collision;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.constraint.Constraint;
import br.edu.ufersa.thingsfall.physics.constraint.ContactConstraint;
import br.edu.ufersa.thingsfall.physics.constraint.RevoluteConstraint;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;
import br.edu.ufersa.thingsfall.physics.shape.PhysicsShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CollisionHelper {
    private static final double FACE_SWITCH_TOLERANCE = 0.0001;
    private static final double LINEAR_SLOP = 0.002;

    // Prevents creating instances because this class only provides collision utilities.
    private CollisionHelper() {}

    // Checks whether two bodies pass the static-body and collision-mask filters.
    public static boolean shouldCollide(PhysicsObject bodyA, PhysicsObject bodyB) {
        return shouldCollide(bodyA, bodyB, List.of());
    }

    // Excludes bodies connected by a revolute constraint while preserving other collision filters.
    public static boolean shouldCollide(PhysicsObject bodyA, PhysicsObject bodyB, List<Constraint> constraints) {
        if (bodyA.isStatic && bodyB.isStatic) return false;
        if ((bodyA.collisionMask & bodyB.collisionMask) == 0) return false;
        if ((bodyA.collisionMaskIgnore & bodyB.collisionMask) == bodyB.collisionMask) return false;
        if ((bodyB.collisionMaskIgnore & bodyA.collisionMask) == bodyA.collisionMask) return false;

        for (Constraint constraint : constraints) {
            if (constraint instanceof RevoluteConstraint
                && ((constraint.bodyA == bodyA && constraint.bodyB == bodyB)
                || (constraint.bodyA == bodyB && constraint.bodyB == bodyA))) {
                return false;
            }
        }

        return true;
    }

    // Finds contacts between all body pairs without reusing previous contacts.
    public static List<ContactConstraint> handleCollisions(List<PhysicsObject> objects) {
        return handleCollisions(objects, new ArrayList<>(), List.of());
    }

    // Finds contacts between all body pairs and reuses matching previous contacts.
    public static List<ContactConstraint> handleCollisions(
        List<PhysicsObject> objects,
        List<ContactConstraint> contactConstraintsForReuse
    ) {
        return handleCollisions(objects, contactConstraintsForReuse, List.of());
    }

    // Finds contacts while excluding body pairs connected by revolute constraints.
    public static List<ContactConstraint> handleCollisions(
        List<PhysicsObject> objects,
        List<ContactConstraint> contactConstraintsForReuse,
        List<Constraint> constraints
    ) {
        List<ContactConstraint> newContactConstraints = new ArrayList<>();

        for (int firstBodyIndex = 0; firstBodyIndex < objects.size(); firstBodyIndex++) {
            for (int secondBodyIndex = firstBodyIndex + 1; secondBodyIndex < objects.size(); secondBodyIndex++) {
                PhysicsObject bodyA = objects.get(firstBodyIndex);
                PhysicsObject bodyB = objects.get(secondBodyIndex);
                newContactConstraints.addAll(checkCollision(bodyA, bodyB, contactConstraintsForReuse, constraints));
            }
        }

        return newContactConstraints;
    }

    // Tests every eligible shape pair and creates or updates its contact constraints.
    public static List<ContactConstraint> checkCollision(
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        List<ContactConstraint> contactConstraintsForReuse
    ) {
        return checkCollision(bodyA, bodyB, contactConstraintsForReuse, List.of());
    }

    // Checks a body pair, skipping it when a revolute constraint connects the bodies.
    public static List<ContactConstraint> checkCollision(
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        List<ContactConstraint> contactConstraintsForReuse,
        List<Constraint> constraints
    ) {
        if (!shouldCollide(bodyA, bodyB, constraints)
            || bodyA.shapes.isEmpty()
            || bodyB.shapes.isEmpty()
            || !bodyA.getAxisAlignedBoundingBox().overlaps(bodyB.getAxisAlignedBoundingBox())
        ) {
            return new ArrayList<>();
        }

        List<ContactConstraint> contactConstraints = new ArrayList<>();
        for (int shapeIndexOnBodyA = 0; shapeIndexOnBodyA < bodyA.shapes.size(); shapeIndexOnBodyA++) {
            PhysicsShape shapeA = bodyA.shapes.get(shapeIndexOnBodyA);

            for (int shapeIndexOnBodyB = 0; shapeIndexOnBodyB < bodyB.shapes.size(); shapeIndexOnBodyB++) {
                PhysicsShape shapeB = bodyB.shapes.get(shapeIndexOnBodyB);
                CollisionInfo collision = findCollision(bodyA, shapeA, bodyB, shapeB);
                if (collision == null || collision.normal == null) {
                    continue;
                }

                Vector2 collisionNormal = orientNormalFromBodyAToBodyB(collision.normal, bodyA, bodyB);
                List<ContactPoint> contactPoints = getContactPoints(
                    bodyA,
                    shapeA,
                    shapeIndexOnBodyA,
                    bodyB,
                    shapeB,
                    shapeIndexOnBodyB,
                    collision,
                    collisionNormal
                );

                for (ContactPoint contactPoint : contactPoints) {
                    ContactConstraint reusedContact =
                        findReusableContact(
                            bodyA,
                            bodyB,
                            contactPoint.collisionFeatureIdentifier,
                            contactConstraintsForReuse
                        );

                    if (reusedContact != null) {
                        reusedContact.isReused = true;
                        reusedContact.setCollisionData(contactPoint.position, collisionNormal, collision.penetration);
                        contactConstraints.add(reusedContact);
                    } else {
                        contactConstraints.add(new ContactConstraint(
                            bodyA,
                            bodyB,
                            contactPoint.position,
                            collisionNormal,
                            collision.penetration,
                            contactPoint.collisionFeatureIdentifier
                        ));
                    }
                }
            }
        }

        return contactConstraints;
    }

    // Selects the appropriate collision test based on the two shape types.
    private static CollisionInfo findCollision(
        PhysicsObject bodyA,
        PhysicsShape shapeA,
        PhysicsObject bodyB,
        PhysicsShape shapeB
    ) {
        if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
            return circleToCircleSeparatingAxisTheorem(
                bodyA,
                (CircleShape) shapeA,
                bodyB,
                (CircleShape) shapeB
            );
        }
        if (shapeA instanceof CircleShape && shapeB instanceof ConvexPolygonShape) {
            return circleToPolygonSeparatingAxisTheorem(
                bodyA,
                (CircleShape) shapeA,
                bodyB,
                (ConvexPolygonShape) shapeB
            );
        }
        if (shapeA instanceof ConvexPolygonShape && shapeB instanceof CircleShape) {
            return reverseCollision(circleToPolygonSeparatingAxisTheorem(
                bodyB,
                (CircleShape) shapeB,
                bodyA,
                (ConvexPolygonShape) shapeA
            ));
        }
        if (shapeA instanceof ConvexPolygonShape && shapeB instanceof ConvexPolygonShape) {
            return polygonToPolygonSeparatingAxisTheorem(
                bodyA,
                (ConvexPolygonShape) shapeA,
                bodyB,
                (ConvexPolygonShape) shapeB
            );
        }
        return null;
    }

    // Reverses collision direction and reference-edge ownership when needed.
    private static CollisionInfo reverseCollision(CollisionInfo collision) {
        if (collision == null) return null;
        return new CollisionInfo(
            collision.normal.scale(-1.0),
            collision.penetration,
            !collision.referenceShapeBelongsToBodyA,
            collision.referenceEdgeIndex
        );
    }

    // Ensures the collision normal points from the first body toward the second.
    private static Vector2 orientNormalFromBodyAToBodyB(Vector2 normal, PhysicsObject bodyA, PhysicsObject bodyB) {
        Vector2 bodyCenterDirection = bodyB.position.subtract(bodyA.position);
        if (normal.dotProduct(bodyCenterDirection) < 0.0) {
            return normal.scale(-1.0);
        }
        return normal;
    }

    // Selects circle contact points or clips polygon faces to find contact points.
    private static List<ContactPoint> getContactPoints(
        PhysicsObject bodyA,
        PhysicsShape shapeA,
        int shapeIndexOnBodyA,
        PhysicsObject bodyB,
        PhysicsShape shapeB,
        int shapeIndexOnBodyB,
        CollisionInfo collision,
        Vector2 collisionNormal
    ) {
        if (shapeA instanceof CircleShape && shapeB instanceof CircleShape) {
            return clipCircleToCircle(
                bodyA,
                (CircleShape) shapeA,
                shapeIndexOnBodyA,
                bodyB,
                (CircleShape) shapeB,
                shapeIndexOnBodyB,
                collisionNormal
            );
        }
        if (shapeA instanceof CircleShape && shapeB instanceof ConvexPolygonShape) {
            return clipCircleToPolygon(
                bodyA,
                (CircleShape) shapeA,
                shapeIndexOnBodyA,
                bodyB,
                (ConvexPolygonShape) shapeB,
                shapeIndexOnBodyB,
                collisionNormal
            );
        }
        if (shapeA instanceof ConvexPolygonShape && shapeB instanceof CircleShape) {
            return clipCircleToPolygon(
                bodyB,
                (CircleShape) shapeB,
                shapeIndexOnBodyB,
                bodyA,
                (ConvexPolygonShape) shapeA,
                shapeIndexOnBodyA,
                collisionNormal
            );
        }

        PhysicsObject referenceBody = collision.referenceShapeBelongsToBodyA ? bodyA : bodyB;
        ConvexPolygonShape referenceShape =
            (ConvexPolygonShape) (collision.referenceShapeBelongsToBodyA ? shapeA : shapeB);
        PhysicsObject incidentBody = collision.referenceShapeBelongsToBodyA ? bodyB : bodyA;
        ConvexPolygonShape incidentShape =
            (ConvexPolygonShape) (collision.referenceShapeBelongsToBodyA ? shapeB : shapeA);
        int referenceShapeIndex = collision.referenceShapeBelongsToBodyA ? shapeIndexOnBodyA : shapeIndexOnBodyB;
        int incidentShapeIndex = collision.referenceShapeBelongsToBodyA ? shapeIndexOnBodyB : shapeIndexOnBodyA;

        return clipPolygonToPolygon(
            referenceBody,
            referenceShape,
            referenceShapeIndex,
            incidentBody,
            incidentShape,
            incidentShapeIndex,
            collision
        );
    }

    // Finds and removes a previous contact with the same bodies and feature identifier.
    private static ContactConstraint findReusableContact(
        PhysicsObject bodyA,
        PhysicsObject bodyB,
        int collisionFeatureIdentifier,
        List<ContactConstraint> contactConstraintsForReuse
    ) {
        for (int contactIndex = 0; contactIndex < contactConstraintsForReuse.size(); contactIndex++) {
            ContactConstraint previousContact = contactConstraintsForReuse.get(contactIndex);
            if (previousContact.bodyA == bodyA
                && previousContact.bodyB == bodyB
                && Objects.equals(previousContact.collisionFeatureIdentifier, collisionFeatureIdentifier)) {
                contactConstraintsForReuse.remove(contactIndex);
                return previousContact;
            }
        }
        return null;
    }

    // Builds outward-facing unit normals from consecutive polygon vertices.
    private static List<Vector2> getPolygonNormals(List<Vector2> worldVertices) {
        List<Vector2> normals = new ArrayList<>();
        for (int vertexIndex = 0; vertexIndex < worldVertices.size(); vertexIndex++) {
            Vector2 startVertex = worldVertices.get(vertexIndex);
            Vector2 endVertex = worldVertices.get((vertexIndex + 1) % worldVertices.size());
            Vector2 edge = endVertex.subtract(startVertex);
            normals.add(normalizeOrDefault(edge.rotate90CW()));
        }
        return normals;
    }

    // Returns the minimum and maximum vertex projections along an axis.
    private static double[] projectVertices(List<Vector2> vertices, Vector2 axis) {
        double minimumProjection = Double.POSITIVE_INFINITY;
        double maximumProjection = Double.NEGATIVE_INFINITY;

        for (Vector2 vertex : vertices) {
            double projection = vertex.dotProduct(axis);
            minimumProjection = Math.min(minimumProjection, projection);
            maximumProjection = Math.max(maximumProjection, projection);
        }

        return new double[] {minimumProjection, maximumProjection};
    }

    // Uses the separating axis theorem to detect polygon overlap and select a reference edge.
    public static CollisionInfo polygonToPolygonSeparatingAxisTheorem(
        PhysicsObject bodyA,
        ConvexPolygonShape shapeA,
        PhysicsObject bodyB,
        ConvexPolygonShape shapeB
    ) {
        List<Vector2> worldVerticesA = getWorldVertices(bodyA, shapeA);
        List<Vector2> worldVerticesB = getWorldVertices(bodyB, shapeB);
        List<Vector2> normalsA = getPolygonNormals(worldVerticesA);
        List<Vector2> normalsB = getPolygonNormals(worldVerticesB);

        double greatestSeparationFromBodyA = Double.NEGATIVE_INFINITY;
        int referenceEdgeOnBodyA = 0;
        double greatestSeparationFromBodyB = Double.NEGATIVE_INFINITY;
        int referenceEdgeOnBodyB = 0;

        // Every face normal is a possible separating axis for the polygons.
        for (int edgeIndex = 0; edgeIndex < normalsA.size(); edgeIndex++) {
            double[] projectionA = projectVertices(worldVerticesA, normalsA.get(edgeIndex));
            double[] projectionB = projectVertices(worldVerticesB, normalsA.get(edgeIndex));
            if (projectionA[0] > projectionB[1] || projectionB[0] > projectionA[1]) return null;

            double separation = projectionB[0] - projectionA[1];
            if (separation > greatestSeparationFromBodyA) {
                greatestSeparationFromBodyA = separation;
                referenceEdgeOnBodyA = edgeIndex;
            }
        }

        for (int edgeIndex = 0; edgeIndex < normalsB.size(); edgeIndex++) {
            double[] projectionA = projectVertices(worldVerticesA, normalsB.get(edgeIndex));
            double[] projectionB = projectVertices(worldVerticesB, normalsB.get(edgeIndex));
            if (projectionA[0] > projectionB[1] || projectionB[0] > projectionA[1]) return null;

            double separation = projectionA[0] - projectionB[1];
            if (separation > greatestSeparationFromBodyB) {
                greatestSeparationFromBodyB = separation;
                referenceEdgeOnBodyB = edgeIndex;
            }
        }

        boolean referenceShapeBelongsToBodyA = true;
        int referenceEdgeIndex = referenceEdgeOnBodyA;
        Vector2 collisionNormal = normalsA.get(referenceEdgeIndex);

        // Keep the selected reference face stable when both candidates are close.
        if (greatestSeparationFromBodyB > greatestSeparationFromBodyA + FACE_SWITCH_TOLERANCE) {
            referenceShapeBelongsToBodyA = false;
            referenceEdgeIndex = referenceEdgeOnBodyB;
            collisionNormal = normalsB.get(referenceEdgeIndex);
        }

        double penetration = referenceShapeBelongsToBodyA ? -greatestSeparationFromBodyA : -greatestSeparationFromBodyB;
        return new CollisionInfo(
            collisionNormal,
            penetration,
            referenceShapeBelongsToBodyA,
            referenceEdgeIndex
        );
    }

    // Tests polygon face normals and the closest-vertex axis for circle-polygon overlap.
    public static CollisionInfo circleToPolygonSeparatingAxisTheorem(
        PhysicsObject circleBody,
        CircleShape circleShape,
        PhysicsObject polygonBody,
        ConvexPolygonShape polygonShape
    ) {
        Vector2 circleCenter = circleBody.localToWorld(circleShape.offset);
        List<Vector2> polygonVertices = getWorldVertices(polygonBody, polygonShape);
        List<Vector2> polygonNormals = getPolygonNormals(polygonVertices);
        double minimumPenetration = Double.POSITIVE_INFINITY;
        Vector2 collisionNormal = null;

        for (Vector2 axis : polygonNormals) {
            double[] polygonProjection = projectVertices(polygonVertices, axis);
            double circleProjection = circleCenter.dotProduct(axis);
            double circleMinimum = circleProjection - circleShape.radius;
            double circleMaximum = circleProjection + circleShape.radius;

            if (polygonProjection[0] > circleMaximum || circleMinimum > polygonProjection[1]) return null;

            double penetration =
                Math.min(polygonProjection[1], circleMaximum) - Math.max(polygonProjection[0], circleMinimum);
            if (penetration < minimumPenetration) {
                minimumPenetration = penetration;
                collisionNormal = axis;
            }
        }

        Vector2 closestVertex = polygonVertices.get(0);
        double minimumVertexDistance = closestVertex.subtract(circleCenter).length();
        for (Vector2 vertex : polygonVertices) {
            double vertexDistance = vertex.subtract(circleCenter).length();
            if (vertexDistance < minimumVertexDistance) {
                closestVertex = vertex;
                minimumVertexDistance = vertexDistance;
            }
        }

        Vector2 vertexAxis = normalizeOrDefault(closestVertex.subtract(circleCenter));
        double[] polygonProjection = projectVertices(polygonVertices, vertexAxis);
        double circleProjection = circleCenter.dotProduct(vertexAxis);
        double circleMinimum = circleProjection - circleShape.radius;
        double circleMaximum = circleProjection + circleShape.radius;
        if (polygonProjection[0] > circleMaximum || circleMinimum > polygonProjection[1]) return null;

        double vertexAxisPenetration =
            Math.min(polygonProjection[1], circleMaximum) - Math.max(polygonProjection[0], circleMinimum);
        if (vertexAxisPenetration < minimumPenetration) {
            minimumPenetration = vertexAxisPenetration;
            collisionNormal = vertexAxis;
        }

        return new CollisionInfo(
            collisionNormal,
            minimumPenetration,
            false,
            -1
        );
    }

    // Compares circle-center distance with the sum of their radii.
    public static CollisionInfo circleToCircleSeparatingAxisTheorem(
        PhysicsObject bodyA,
        CircleShape shapeA,
        PhysicsObject bodyB,
        CircleShape shapeB
    ) {
        Vector2 worldCenterA = bodyA.localToWorld(shapeA.offset);
        Vector2 worldCenterB = bodyB.localToWorld(shapeB.offset);
        Vector2 centerDifference = worldCenterB.subtract(worldCenterA);
        double centerDistance = centerDifference.length();
        double combinedRadius = shapeA.radius + shapeB.radius;

        if (centerDistance > combinedRadius) return null;

        return new CollisionInfo(
            normalizeOrDefault(centerDifference),
            combinedRadius - centerDistance,
            true,
            -1
        );
    }

    // Clips a segment against a line and adds an intersection when its endpoints straddle it.
    private static List<Vector2> clipLineSegmentToLine(
        Vector2 firstPoint,
        Vector2 secondPoint,
        Vector2 normal,
        Vector2 lineOffset
    ) {
        List<Vector2> clippedPoints = new ArrayList<>();
        double firstDistance = firstPoint.subtract(lineOffset).dotProduct(normal);
        double secondDistance = secondPoint.subtract(lineOffset).dotProduct(normal);

        if (firstDistance <= 0.0) {
            clippedPoints.add(firstPoint);
        }
        if (secondDistance <= 0.0) {
            clippedPoints.add(secondPoint);
        }

        if (Math.signum(firstDistance) != Math.signum(secondDistance) && clippedPoints.size() < 2) {
            double percentageAcross = secondDistance / (secondDistance - firstDistance);
            Vector2 intersectionPoint = secondPoint.add(firstPoint.subtract(secondPoint).scale(percentageAcross));
            clippedPoints.add(intersectionPoint);
        }

        return clippedPoints;
    }

    // Clips the incident polygon edge to the reference face and assigns stable feature identifiers.
    private static List<ContactPoint> clipPolygonToPolygon(
        PhysicsObject referenceBody,
        ConvexPolygonShape referenceShape,
        int referenceShapeIndex,
        PhysicsObject incidentBody,
        ConvexPolygonShape incidentShape,
        int incidentShapeIndex,
        CollisionInfo collision
    ) {
        List<Vector2> referenceVertices = getWorldVertices(referenceBody, referenceShape);
        List<Vector2> incidentVertices = getWorldVertices(incidentBody, incidentShape);
        List<Vector2> referenceNormals = getPolygonNormals(referenceVertices);
        List<Vector2> incidentNormals = getPolygonNormals(incidentVertices);

        int referenceEdgeIndex = collision.referenceEdgeIndex;
        Vector2 referenceEdgeStart = referenceVertices.get(referenceEdgeIndex);
        Vector2 referenceEdgeEnd = referenceVertices.get((referenceEdgeIndex + 1) % referenceVertices.size());
        Vector2 referenceNormal = referenceNormals.get(referenceEdgeIndex);

        int incidentEdgeIndex = 0;
        double lowestNormalDotProduct = Double.POSITIVE_INFINITY;
        for (int edgeIndex = 0; edgeIndex < incidentNormals.size(); edgeIndex++) {
            double normalDotProduct = referenceNormal.dotProduct(incidentNormals.get(edgeIndex));
            if (normalDotProduct < lowestNormalDotProduct) {
                lowestNormalDotProduct = normalDotProduct;
                incidentEdgeIndex = edgeIndex;
            }
        }

        Vector2 incidentEdgeEnd = incidentVertices.get(incidentEdgeIndex);
        Vector2 incidentEdgeStart = incidentVertices.get((incidentEdgeIndex + 1) % incidentVertices.size());
        Vector2 referenceTangent = normalizeOrDefault(referenceEdgeEnd.subtract(referenceEdgeStart));

        List<Vector2> clippedPoints = clipLineSegmentToLine(
            incidentEdgeStart,
            incidentEdgeEnd,
            referenceTangent.scale(-1.0),
            referenceEdgeStart
        );
        if (clippedPoints.size() < 2) {
            return new ArrayList<>();
        }

        clippedPoints = clipLineSegmentToLine(
            clippedPoints.get(0),
            clippedPoints.get(1),
            referenceTangent,
            referenceEdgeEnd
        );

        List<ContactPoint> contactPoints = new ArrayList<>();
        int referenceEdgeEndIndex = (referenceEdgeIndex + 1) % referenceVertices.size();
        int incidentEdgeStartIndex = (incidentEdgeIndex + 1) % incidentVertices.size();
        int featureIdentifierPrefix = ((referenceBody.identifier & 0xFF) << 24)
            | ((incidentBody.identifier & 0xFF) << 16)
            | ((referenceShapeIndex & 0xF) << 12)
            | ((incidentShapeIndex & 0xF) << 8);

        for (Vector2 point : clippedPoints) {
            if (referenceNormal.dotProduct(point.subtract(referenceEdgeStart)) > LINEAR_SLOP) {
                continue;
            }

            int pointIndex = contactPoints.size();
            int vertexFeatureBits = pointIndex == 0
                ? ((referenceEdgeIndex & 0xF) << 4)
                | (incidentEdgeIndex & 0xF)
                : ((referenceEdgeEndIndex & 0xF) << 4)
                | (incidentEdgeStartIndex & 0xF);
            contactPoints.add(new ContactPoint(point, featureIdentifierPrefix | vertexFeatureBits));
        }

        return contactPoints;
    }

    // Places the circle contact point along the collision normal and identifies its shapes.
    private static List<ContactPoint> clipCircleToPolygon(
        PhysicsObject circleBody,
        CircleShape circleShape,
        int circleShapeIndex,
        PhysicsObject polygonBody,
        ConvexPolygonShape polygonShape,
        int polygonShapeIndex,
        Vector2 collisionNormal
    ) {
        Vector2 circleCenter = circleBody.localToWorld(circleShape.offset);
        Vector2 directionToPolygon = polygonBody.position.subtract(circleCenter);
        double directionSign = Math.signum(collisionNormal.dotProduct(directionToPolygon));
        if (directionSign == 0.0) {
            directionSign = 1.0;
        }

        Vector2 contactPoint = circleCenter.add(collisionNormal.scale(circleShape.radius * directionSign));
        int collisionFeatureIdentifier = createCircleFeatureIdentifier(
            circleBody,
            circleShapeIndex,
            polygonBody,
            polygonShapeIndex
        );
        return List.of(new ContactPoint(contactPoint, collisionFeatureIdentifier));
    }

    // Uses the point on the first circle along the normal as the contact point.
    private static List<ContactPoint> clipCircleToCircle(
        PhysicsObject bodyA,
        CircleShape shapeA,
        int shapeIndexOnBodyA,
        PhysicsObject bodyB,
        CircleShape shapeB,
        int shapeIndexOnBodyB,
        Vector2 collisionNormal
    ) {
        Vector2 worldCenterA = bodyA.localToWorld(shapeA.offset);
        Vector2 contactPoint = worldCenterA.add(collisionNormal.scale(shapeA.radius));
        int collisionFeatureIdentifier = createCircleFeatureIdentifier(
            bodyA,
            shapeIndexOnBodyA,
            bodyB,
            shapeIndexOnBodyB
        );
        return List.of(new ContactPoint(contactPoint, collisionFeatureIdentifier));
    }

    // Packs body and shape identifiers into the feature key used to persist circle contacts.
    private static int createCircleFeatureIdentifier(
        PhysicsObject circleBody,
        int circleShapeIndex,
        PhysicsObject otherBody,
        int otherShapeIndex
    ) {
        return ((circleBody.identifier & 0xFF) << 24)
            | ((otherBody.identifier & 0xFF) << 16)
            | ((circleShapeIndex & 0xFF) << 8)
            | (otherShapeIndex & 0xFF);
    }

    // Transforms each polygon vertex from body-local space to world space.
    private static List<Vector2> getWorldVertices(PhysicsObject body, ConvexPolygonShape shape) {
        List<Vector2> worldVertices = new ArrayList<>();
        for (Vector2 localVertex : shape.vertices) {
            worldVertices.add(body.localToWorld(localVertex));
        }
        return worldVertices;
    }

    // Normalizes a vector, using a fixed direction when its length is nearly zero.
    private static Vector2 normalizeOrDefault(Vector2 vector) {
        double vectorLength = vector.length();
        if (vectorLength < 1e-10) {
            return new Vector2(1.0, 0.0);
        }
        return vector.scale(1.0 / vectorLength);
    }

    private static class ContactPoint {
        private final Vector2 position;
        private final int collisionFeatureIdentifier;

        // Stores a world-space contact point and the identifier used to persist it.
        private ContactPoint(Vector2 position, int collisionFeatureIdentifier) {
            this.position = position;
            this.collisionFeatureIdentifier = collisionFeatureIdentifier;
        }
    }
}
