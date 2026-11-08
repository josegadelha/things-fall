package br.edu.ufersa.thingsfall.physics.collision;

import br.edu.ufersa.thingsfall.math.Vector2;

public class CollisionInfo {
    public final Vector2 normal;
    public final double penetration;
    public final boolean referenceShapeBelongsToBodyA;
    public final int referenceEdgeIndex;

    public CollisionInfo(
        Vector2 normal,
        double penetration,
        boolean referenceShapeBelongsToBodyA,
        int referenceEdgeIndex
    ) {
        this.normal = normal;
        this.penetration = penetration;
        this.referenceShapeBelongsToBodyA = referenceShapeBelongsToBodyA;
        this.referenceEdgeIndex = referenceEdgeIndex;
    }
}
