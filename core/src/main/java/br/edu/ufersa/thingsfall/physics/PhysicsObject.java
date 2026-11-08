package br.edu.ufersa.thingsfall.physics;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.collision.AxisAlignedBoundingBox;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;
import br.edu.ufersa.thingsfall.physics.shape.PhysicsShape;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhysicsObject {
    private static int nextIdentifier = 1;
    private static float nextColorHue = (float) Math.random();

    public final int identifier;
    public Color color;
    public Vector2 position;
    public Vector2 velocity;
    public double rotation;
    public double angularVelocity;
    public boolean isStatic;
    public double mass;
    public double momentOfInertia;
    public double friction = 0.6;
    public double restitution = 0.05;
    public boolean renderDetails = true;
    public int collisionMask = 0xFFFFFF;
    public int collisionMaskIgnore = 0x000000;
    public final List<PhysicsShape> shapes;

    public PhysicsObject(
        double x,
        double y,
        boolean isStatic,
        double mass,
        double momentOfInertia,
        PhysicsShape... shapes
    ) {
        this(x, y, isStatic, mass, momentOfInertia, nextColor(), true, shapes);
    }

    public PhysicsObject(
        double x,
        double y,
        boolean isStatic,
        double mass,
        double momentOfInertia,
        Color color,
        boolean collisionsEnabled,
        PhysicsShape... shapes
    ) {
        this.identifier = nextIdentifier++;
        this.color = new Color(color);
        this.collisionMask = collisionsEnabled ? 0xFFFFFF : 0;

        if (!isStatic) {
            if (!Double.isFinite(mass) || mass <= 0) {
                throw new IllegalArgumentException("Mass must be finite and positive");
            }
            if (!Double.isFinite(momentOfInertia) || momentOfInertia <= 0) {
                throw new IllegalArgumentException("Moment of inertia must be finite and positive");
            }
        }

        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.rotation = 0;
        this.angularVelocity = 0;
        this.isStatic = isStatic;
        this.mass = isStatic ? Double.POSITIVE_INFINITY : mass;
        this.momentOfInertia = isStatic ? Double.POSITIVE_INFINITY : momentOfInertia;
        this.shapes = new ArrayList<>(Arrays.asList(shapes));
    }

    private static Color nextColor() {
        Color color = new Color().fromHsv(nextColorHue * 360f, 0.25f, 0.98f);
        nextColorHue = (nextColorHue + 0.618034f) % 1f;
        return color;
    }

    public AxisAlignedBoundingBox getAxisAlignedBoundingBox() {
        AxisAlignedBoundingBox boundingBox = new AxisAlignedBoundingBox(
            new Vector2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
            new Vector2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
        );

        for (PhysicsShape shape : shapes) {
            if (shape instanceof CircleShape circleShape) {
                Vector2 worldCenter = localToWorld(circleShape.offset);
                Vector2 radius = new Vector2(circleShape.radius, circleShape.radius);
                boundingBox.expandToInclude(worldCenter.subtract(radius));
                boundingBox.expandToInclude(worldCenter.add(radius));
            } else if (shape instanceof ConvexPolygonShape polygonShape) {
                for (Vector2 vertex : polygonShape.vertices) {
                    boundingBox.expandToInclude(localToWorld(vertex));
                }
            }
        }

        return boundingBox;
    }

    public void step(double dt) {
        if (isStatic) return;

        // Integrate position and rotation based on velocities and timestep
        this.position = this.position.add(this.velocity.scale(dt));
        this.rotation += this.angularVelocity * dt;
    }

    public Vector2 localToWorld(Vector2 localPoint) {
        // Calculate cosine and sine of the object's rotation angle
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        // Rotate the local point and add the object's world X and Y positions
        return new Vector2(
            position.x + localPoint.x * cos - localPoint.y * sin,
            position.y + localPoint.x * sin + localPoint.y * cos
        );
    }

    public Vector2 worldToLocal(Vector2 worldPoint) {
        // Calculate cosine and sine of the inverse rotation angle
        double cos = Math.cos(-rotation);
        double sin = Math.sin(-rotation);

        // Subtract the object's world position to get the point relative to its origin
        Vector2 relativePoint = worldPoint.subtract(position);

        // Apply the inverse rotation to calculate the local X and Y coordinates
        return new Vector2(
            relativePoint.x * cos - relativePoint.y * sin,
            relativePoint.x * sin + relativePoint.y * cos
        );
    }

    public void applyImpulse(Vector2 impulse, Vector2 contactPoint) {
        if (isStatic) return;

        Vector2 contactOffset = contactPoint.subtract(this.position); // Vector from center of the object to contact point
        double angularImpulse = contactOffset.crossProduct(impulse); // Calculate the torque generated by the impulse at the contact point

        // Update linear velocity based on the force and the object's mass
        this.velocity = this.velocity.add(impulse.scale(1.0 / this.mass));
        // Update angular velocity based on the torque and moment of inertia
        this.angularVelocity += angularImpulse / this.momentOfInertia;
    }
}
