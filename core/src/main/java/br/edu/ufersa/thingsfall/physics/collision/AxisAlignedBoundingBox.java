package br.edu.ufersa.thingsfall.physics.collision;

import br.edu.ufersa.thingsfall.math.Vector2;

public class AxisAlignedBoundingBox {
    public Vector2 minimum;
    public Vector2 maximum;

    public AxisAlignedBoundingBox(Vector2 minimum, Vector2 maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public boolean overlaps(AxisAlignedBoundingBox other) {
        return minimum.x <= other.maximum.x
            && maximum.x >= other.minimum.x
            && minimum.y <= other.maximum.y
            && maximum.y >= other.minimum.y;
    }

    public void expandToInclude(Vector2 point) {
        minimum = new Vector2(
            Math.min(minimum.x, point.x),
            Math.min(minimum.y, point.y)
        );
        maximum = new Vector2(
            Math.max(maximum.x, point.x),
            Math.max(maximum.y, point.y)
        );
    }
}
