package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.math.Vector2;
import com.badlogic.gdx.graphics.Color;

import java.util.List;

public final class Geometry2 extends GeometryBase {
    public Geometry2() {
        this(Color.valueOf("a0a0a0"));
    }

    public Geometry2(Color color) {
        super(color, List.of(
        new Polygon(-4, 0, List.of(
            new Vector2(0.0, 0.8),
            new Vector2(0.65, 0.8),
            new Vector2(0.65, 2.0),
            new Vector2(0.3, 2.0),
            new Vector2(0.0, 1.6)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(0.65, 1.65),
            new Vector2(2.05, 1.65),
            new Vector2(2.05, 2.25),
            new Vector2(1.2, 2.25),
            new Vector2(0.65, 2.05)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(2.05, 0.5),
            new Vector2(5.95, 0.5),
            new Vector2(5.95, 2.25),
            new Vector2(2.05, 2.25)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(5.95, 1.65),
            new Vector2(7.35, 1.65),
            new Vector2(7.35, 2.05),
            new Vector2(6.8, 2.25),
            new Vector2(5.95, 2.25)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(7.35, 0.8),
            new Vector2(8.0, 0.8),
            new Vector2(8.0, 1.5),
            new Vector2(7.75, 1.9),
            new Vector2(7.35, 1.9)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(2.0, 2.25),
            new Vector2(2.6, 2.25),
            new Vector2(3.1, 3.2),
            new Vector2(2.8, 3.2)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(3.9, 2.25),
            new Vector2(4.3, 2.25),
            new Vector2(4.3, 3.2),
            new Vector2(3.9, 3.2)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(5.5, 2.25),
            new Vector2(6.2, 2.25),
            new Vector2(5.6, 3.2),
            new Vector2(5.2, 3.2)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(2.8, 3.2),
            new Vector2(5.6, 3.2),
            new Vector2(5.3, 3.4),
            new Vector2(3.1, 3.4)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(0.2, 2.0),
            new Vector2(0.4, 2.0),
            new Vector2(0.4, 2.45),
            new Vector2(0.2, 2.45)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(-0.1, 2.45),
            new Vector2(0.9, 2.45),
            new Vector2(0.8, 2.6),
            new Vector2(-0.1, 2.6)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(7.95, 1.1),
            new Vector2(8.1, 1.2),
            new Vector2(8.1, 1.45),
            new Vector2(7.95, 1.5)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(-0.1, 1.2),
            new Vector2(0.05, 1.2),
            new Vector2(0.05, 1.5),
            new Vector2(-0.1, 1.45)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(3.4, 1.7),
            new Vector2(3.8, 1.7),
            new Vector2(3.8, 1.8),
            new Vector2(3.4, 1.8)
        )),
        new Polygon(-4, 0, List.of(
            new Vector2(5.65, 2.3),
            new Vector2(5.95, 2.3),
            new Vector2(5.9, 2.5),
            new Vector2(5.7, 2.5)
        )),
        new Circle(-2.65, 0.7, 0.7),
        new Circle(2.65, 0.7, 0.7),
        new Circle(-2.65, 0.7, 0.45),
        new Circle(2.65, 0.7, 0.45)
        ));
    }
}
