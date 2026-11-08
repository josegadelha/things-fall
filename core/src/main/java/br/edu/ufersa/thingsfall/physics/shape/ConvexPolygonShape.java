package br.edu.ufersa.thingsfall.physics.shape;

import br.edu.ufersa.thingsfall.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConvexPolygonShape implements PhysicsShape {
    public final List<Vector2> vertices;

    public ConvexPolygonShape(Vector2... vertices) {
        if (vertices.length < 3) {
            throw new IllegalArgumentException("A polygon must have at least three vertices");
        }

        validateConvex(vertices);
        this.vertices = new ArrayList<>(Arrays.asList(vertices));
    }

    private static void validateConvex(Vector2[] vertices) {
        boolean hasPositiveTurn = false;
        boolean hasNegativeTurn = false;

        for (int i = 0; i < vertices.length; i++) {
            Vector2 a = vertices[i];
            Vector2 b = vertices[(i + 1) % vertices.length];
            Vector2 c = vertices[(i + 2) % vertices.length];
            double cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
            hasPositiveTurn |= cross > 0;
            hasNegativeTurn |= cross < 0;

            if (hasPositiveTurn && hasNegativeTurn) {
                throw new IllegalArgumentException("Polygon vertices must form a convex shape");
            }
        }

        if (!hasPositiveTurn && !hasNegativeTurn) {
            throw new IllegalArgumentException("Polygon vertices must not be collinear");
        }
    }
}
