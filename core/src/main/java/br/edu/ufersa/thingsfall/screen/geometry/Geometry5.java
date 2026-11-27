package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.math.Vector2;
import com.badlogic.gdx.graphics.Color;

import br.edu.ufersa.thingsfall.common.DynamicArray;

public final class Geometry5 extends GeometryBase {
    public Geometry5() {
        this(Color.valueOf("a0a0a0"));
    }

    public Geometry5(Color color) {
        super(color, DynamicArray.of(
            // Ground
            new Polygon(-25.6, -0.2, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(51.2, 0),
                new Vector2(51.2, 0.2),
                new Vector2(0, 0.2)
            )),

            // Left vertical wall
            new Polygon(-22.1, 0, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.2, 0),
                new Vector2(0.2, 16.7),
                new Vector2(0, 16.7)
            )),

            // Second vertical wall
            new Polygon(-9.3, 0, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.2, 0),
                new Vector2(0.2, 16.4),
                new Vector2(0, 16.4)
            )),

            // Third vertical wall
            new Polygon(5.2, 0, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.2, 0),
                new Vector2(0.2, 15.8),
                new Vector2(0, 15.8)
            )),

            // Right vertical wall
            new Polygon(17.5, 0, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.2, 0),
                new Vector2(0.2, 15.45),
                new Vector2(0, 15.45)
            )),

            // Upper horizontal platform
            new Polygon(5.3, 12.3, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(7.5, 0),
                new Vector2(7.5, 0.2),
                new Vector2(0, 0.2)
            )),

            // Lower horizontal platform
            new Polygon(10.45, 6.05, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(7.15, 0),
                new Vector2(7.15, 0.2),
                new Vector2(0, 0.2)
            )),

            // Row of circles resting along the upper section
            new Circle(-8.3, 15.35, 1),
            new Circle(-6.75, 14.65, 1),
            new Circle(-5.2, 14.0, 1),
            new Circle(-3.45, 13.55, 1),
            new Circle(-1.75, 13.55, 1),
            new Circle(0.0, 13.7, 1),
            new Circle(1.9, 14.0, 1),
            new Circle(4.0, 14.65, 1.2)
        ));
    }
}
