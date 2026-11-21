package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.common.DynamicArray;
import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;
import br.edu.ufersa.thingsfall.physics.shape.PhysicsShape;
import com.badlogic.gdx.graphics.Color;

public abstract class GeometryBase {
    private final DynamicArray<GeometryObject> objects;
    private final Color color;
    private final double offsetX;
    private final double offsetY;

    protected GeometryBase(Color color, DynamicArray<GeometryObject> objects) {
        this(color, objects, 0, 0);
    }

    protected GeometryBase(Color color, DynamicArray<GeometryObject> objects, double offsetX, double offsetY) {
        this.objects = DynamicArray.copyOf(objects);
        this.color = new Color(color);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public PhysicsObject[] spawnObjects() {
        PhysicsObject[] spawnedObjects = new PhysicsObject[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            PhysicsObject spawnedObject = objects.get(i).spawn(color);
            spawnedObject.position = spawnedObject.position.add(new Vector2(offsetX, offsetY));
            spawnedObjects[i] = spawnedObject;
        }
        return spawnedObjects;
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
        DynamicArray<Vector2> vertices,
        Color colorOverride,
        boolean collisionsEnabled
    ) implements GeometryObject {
        protected Polygon(double x, double y, DynamicArray<Vector2> vertices) {
            this(x, y, vertices, null, true);
        }

        protected Polygon(double x, double y, DynamicArray<Vector2> vertices, Color colorOverride) {
            this(x, y, vertices, colorOverride, true);
        }

        protected Polygon {
            vertices = DynamicArray.copyOf(vertices);
            colorOverride = colorOverride == null ? null : new Color(colorOverride);
        }

        @Override
        public PhysicsObject spawn(Color color) {
            Vector2[] verticesCopy = new Vector2[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                Vector2 vertex = vertices.get(i);
                verticesCopy[i] = new Vector2(vertex.x, vertex.y);
            }
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
