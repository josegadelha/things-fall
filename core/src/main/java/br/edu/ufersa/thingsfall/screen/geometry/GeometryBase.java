package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;
import br.edu.ufersa.thingsfall.physics.shape.PhysicsShape;
import com.badlogic.gdx.graphics.Color;

import java.util.List;

public abstract class GeometryBase {
    private final List<GeometryObject> objects;
    private final Color color;
    private final double offsetX;
    private final double offsetY;

    protected GeometryBase(Color color, List<GeometryObject> objects) {
        this(color, objects, 0, 0);
    }

    protected GeometryBase(Color color, List<GeometryObject> objects, double offsetX, double offsetY) {
        this.objects = List.copyOf(objects);
        this.color = new Color(color);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public PhysicsObject[] spawnObjects() {
        return objects.stream()
            .map(object -> {
                PhysicsObject spawnedObject = object.spawn(color);
                spawnedObject.position = spawnedObject.position.add(new Vector2(offsetX, offsetY));
                return spawnedObject;
            })
            .toArray(PhysicsObject[]::new);
    }

    private static PhysicsObject spawnStaticObject(
        double x,
        double y,
        PhysicsShape shape,
        Color color,
        boolean collisionsEnabled
    ) {
        PhysicsObject object = new PhysicsObject(
            x,
            y,
            true,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            color,
            collisionsEnabled,
            shape
        );
        object.renderDetails = false;
        return object;
    }

    protected interface GeometryObject {
        PhysicsObject spawn(Color color);
    }

    protected record Polygon(
        double x,
        double y,
        List<Vector2> vertices,
        Color colorOverride,
        boolean collisionsEnabled
    ) implements GeometryObject {
        protected Polygon(double x, double y, List<Vector2> vertices) {
            this(x, y, vertices, null, true);
        }

        protected Polygon(double x, double y, List<Vector2> vertices, Color colorOverride) {
            this(x, y, vertices, colorOverride, true);
        }

        protected Polygon {
            vertices = List.copyOf(vertices);
            colorOverride = colorOverride == null ? null : new Color(colorOverride);
        }

        @Override
        public PhysicsObject spawn(Color color) {
            Vector2[] verticesCopy = vertices.stream()
                .map(vertex -> new Vector2(vertex.x, vertex.y))
                .toArray(Vector2[]::new);
            return spawnStaticObject(
                x,
                y,
                new ConvexPolygonShape(verticesCopy),
                colorOverride == null ? color : colorOverride,
                collisionsEnabled
            );
        }
    }

    protected record Circle(double x, double y, double radius) implements GeometryObject {
        @Override
        public PhysicsObject spawn(Color color) {
            return spawnStaticObject(x, y, new CircleShape(radius), color, true);
        }
    }
}
