package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.physics.collision.AxisAlignedBoundingBox;
import com.badlogic.gdx.graphics.Color;

import java.util.List;

public final class VictoryArea extends GeometryBase {
    private static final Color BASE_COLOR = Color.valueOf("ff8787");
    private static final Color FLAGPOLE_COLOR = Color.valueOf("c9c9c9");
    private static final Color FLAG_BLACK = Color.valueOf("555560");
    private static final Color FLAG_WHITE = Color.valueOf("f5f5f0");
    private static final double WIDTH = 8.0;
    private static final double HEIGHT = 4.0;

    private final AxisAlignedBoundingBox collisionArea;

    public VictoryArea() {
        this(0, 0, BASE_COLOR);
    }

    public VictoryArea(Color color) {
        this(0, 0, color);
    }

    public VictoryArea(double x, double y) {
        this(x, y, BASE_COLOR);
    }

    public VictoryArea(double x, double y, Color color) {
        super(color, List.of(
            // platform
            new Polygon(0, 0, List.of(new Vector2(0.0, -0.6), new Vector2(8.0, -0.6), new Vector2(8.0, 0.0), new Vector2(0.0, 0.0))),

            // zigzag teeth under the platform
            new Polygon(0, 0, List.of(new Vector2(0.0, -0.6), new Vector2(0.25, -0.9), new Vector2(0.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(0.5, -0.6), new Vector2(0.75, -0.9), new Vector2(1.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(1.0, -0.6), new Vector2(1.25, -0.9), new Vector2(1.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(1.5, -0.6), new Vector2(1.75, -0.9), new Vector2(2.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(2.0, -0.6), new Vector2(2.25, -0.9), new Vector2(2.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(2.5, -0.6), new Vector2(2.75, -0.9), new Vector2(3.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(3.0, -0.6), new Vector2(3.25, -0.9), new Vector2(3.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(3.5, -0.6), new Vector2(3.75, -0.9), new Vector2(4.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(4.0, -0.6), new Vector2(4.25, -0.9), new Vector2(4.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(4.5, -0.6), new Vector2(4.75, -0.9), new Vector2(5.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(5.0, -0.6), new Vector2(5.25, -0.9), new Vector2(5.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(5.5, -0.6), new Vector2(5.75, -0.9), new Vector2(6.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(6.0, -0.6), new Vector2(6.25, -0.9), new Vector2(6.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(6.5, -0.6), new Vector2(6.75, -0.9), new Vector2(7.0, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(7.0, -0.6), new Vector2(7.25, -0.9), new Vector2(7.5, -0.6))),
            new Polygon(0, 0, List.of(new Vector2(7.5, -0.6), new Vector2(7.75, -0.9), new Vector2(8.0, -0.6))),

            // flag pole
            new Polygon(0, 0, List.of(new Vector2(0.10, 0.0), new Vector2(0.26, 0.0), new Vector2(0.26, 4.0), new Vector2(0.10, 4.0)), FLAGPOLE_COLOR, false),

            // checkered flag (black squares only), row 1
            new Polygon(0, 0, List.of(new Vector2(0.26, 3.6), new Vector2(0.56, 3.6), new Vector2(0.56, 3.9), new Vector2(0.26, 3.9)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(0.86, 3.6), new Vector2(1.16, 3.6), new Vector2(1.16, 3.9), new Vector2(0.86, 3.9)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(1.46, 3.6), new Vector2(1.76, 3.6), new Vector2(1.76, 3.9), new Vector2(1.46, 3.9)), FLAG_BLACK, false),
            // row 2
            new Polygon(0, 0, List.of(new Vector2(0.56, 3.3), new Vector2(0.86, 3.3), new Vector2(0.86, 3.6), new Vector2(0.56, 3.6)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(1.16, 3.3), new Vector2(1.46, 3.3), new Vector2(1.46, 3.6), new Vector2(1.16, 3.6)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(1.76, 3.3), new Vector2(2.06, 3.3), new Vector2(2.06, 3.6), new Vector2(1.76, 3.6)), FLAG_BLACK, false),
            // row 3
            new Polygon(0, 0, List.of(new Vector2(0.26, 3.0), new Vector2(0.56, 3.0), new Vector2(0.56, 3.3), new Vector2(0.26, 3.3)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(0.86, 3.0), new Vector2(1.16, 3.0), new Vector2(1.16, 3.3), new Vector2(0.86, 3.3)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(1.46, 3.0), new Vector2(1.76, 3.0), new Vector2(1.76, 3.3), new Vector2(1.46, 3.3)), FLAG_BLACK, false),
            // row 4
            new Polygon(0, 0, List.of(new Vector2(0.56, 2.7), new Vector2(0.86, 2.7), new Vector2(0.86, 3.0), new Vector2(0.56, 3.0)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(1.16, 2.7), new Vector2(1.46, 2.7), new Vector2(1.46, 3.0), new Vector2(1.16, 3.0)), FLAG_BLACK, false),
            new Polygon(0, 0, List.of(new Vector2(1.76, 2.7), new Vector2(2.06, 2.7), new Vector2(2.06, 3.0), new Vector2(1.76, 3.0)), FLAG_BLACK, false),

            // checkered flag (the other squares), row 1
            new Polygon(0, 0, List.of(new Vector2(0.56, 3.6), new Vector2(0.86, 3.6), new Vector2(0.86, 3.9), new Vector2(0.56, 3.9)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(1.16, 3.6), new Vector2(1.46, 3.6), new Vector2(1.46, 3.9), new Vector2(1.16, 3.9)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(1.76, 3.6), new Vector2(2.06, 3.6), new Vector2(2.06, 3.9), new Vector2(1.76, 3.9)), FLAG_WHITE, false),
            // row 2
            new Polygon(0, 0, List.of(new Vector2(0.26, 3.3), new Vector2(0.56, 3.3), new Vector2(0.56, 3.6), new Vector2(0.26, 3.6)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(0.86, 3.3), new Vector2(1.16, 3.3), new Vector2(1.16, 3.6), new Vector2(0.86, 3.6)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(1.46, 3.3), new Vector2(1.76, 3.3), new Vector2(1.76, 3.6), new Vector2(1.46, 3.6)), FLAG_WHITE, false),
            // row 3
            new Polygon(0, 0, List.of(new Vector2(0.56, 3.0), new Vector2(0.86, 3.0), new Vector2(0.86, 3.3), new Vector2(0.56, 3.3)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(1.16, 3.0), new Vector2(1.46, 3.0), new Vector2(1.46, 3.3), new Vector2(1.16, 3.3)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(1.76, 3.0), new Vector2(2.06, 3.0), new Vector2(2.06, 3.3), new Vector2(1.76, 3.3)), FLAG_WHITE, false),
            // row 4
            new Polygon(0, 0, List.of(new Vector2(0.26, 2.7), new Vector2(0.56, 2.7), new Vector2(0.56, 3.0), new Vector2(0.26, 3.0)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(0.86, 2.7), new Vector2(1.16, 2.7), new Vector2(1.16, 3.0), new Vector2(0.86, 3.0)), FLAG_WHITE, false),
            new Polygon(0, 0, List.of(new Vector2(1.46, 2.7), new Vector2(1.76, 2.7), new Vector2(1.76, 3.0), new Vector2(1.46, 3.0)), FLAG_WHITE, false)
        ), x, y);
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Victory area position must be finite");
        }
        collisionArea = new AxisAlignedBoundingBox(
            new Vector2(x, y),
            new Vector2(x + WIDTH, y + HEIGHT)
        );
    }

    public boolean isReached(PhysicsWorld world) {
        if (world.objects.isEmpty()) {
            return false;
        }

        PhysicsObject player = world.objects.getFirst();
        return collisionArea.overlaps(player.getAxisAlignedBoundingBox());
    }
}
