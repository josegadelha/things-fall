
package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import br.edu.ufersa.thingsfall.common.DynamicArray;

public final class Geometry4 extends GeometryBase {

    public Geometry4() {
        this(Color.valueOf("a0a0a0"));
    }

    public Geometry4(Color color) {
        super(color, DynamicArray.of(
            // Roof: left slope
            new Polygon(-22, 0, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(22, 13.1),
                new Vector2(21.85, 13.35),
                new Vector2(-0.15, 0.25)
            )),

            // Roof: right slope
            /*
            new Polygon(0, 0, DynamicArray.of(
                new Vector2(22, 13.1),
                new Vector2(22.15, 12.85),
                new Vector2(0.15, 0),
                new Vector2(0, 0.25)
            )), */

            // Roof: horizontal base
            new Polygon(-22, -0.25, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(44, 0),
                new Vector2(44, 0.25),
                new Vector2(0, 0.25)
            )),

            // Left exterior wall
            new Polygon(-16.6, -26, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.3, 0),
                new Vector2(0.3, 26),
                new Vector2(0, 26)
            )),

            // Right exterior wall
            new Polygon(16.8, -26, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.3, 0),
                new Vector2(0.3, 26),
                new Vector2(0, 26)
            )),

            // Attic circular window
            new Circle(0, 5.4, 3.6),

            // Window: vertical division
            new Polygon(-0.15, 0.9, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.3, 0),
                new Vector2(0.3, 9),
                new Vector2(0, 9)
            )),

            // Window: horizontal division
            new Polygon(-4.4, 5.25, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(8.8, 0),
                new Vector2(8.8, 0.3),
                new Vector2(0, 0.3)
            )),

            // Door: left side
            new Polygon(-3.8, -26, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.3, 0),
                new Vector2(0.3, 11.2),
                new Vector2(0, 11.2)
            )),

            // Door: right side
            new Polygon(3.1, -26, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(0.3, 0),
                new Vector2(0.3, 11.2),
                new Vector2(0, 11.2)
            )),

            // Door: upper horizontal edge
            new Polygon(-3.8, -14.8, DynamicArray.of(
                new Vector2(0, 0),
                new Vector2(7.2, 0),
                new Vector2(7.2, 0.3),
                new Vector2(0, 0.3)
            )),

            // Door knob
            new Circle(-2.65, -21.7, 0.75)

        ));
    }
}
