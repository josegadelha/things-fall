package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.math.Vector2;
import com.badlogic.gdx.graphics.Color;

import java.util.List;

public final class Geometry1 extends GeometryBase {
    public Geometry1() {
        this(Color.valueOf("a0a0a0"));
    }

    public Geometry1(Color color) {
        super(color, List.of(
        new Polygon(-22, -2, List.of(
            new Vector2(0, 0),
            new Vector2(44, 0),
            new Vector2(44, 2),
            new Vector2(0, 2)
        )),
        new Polygon(-18, -3, List.of(
            new Vector2(0, 1.5),
            new Vector2(7, 6.5),
            new Vector2(7, 8),
            new Vector2(0, 3)
        )),
        new Polygon(-11, 2, List.of(
            new Vector2(0, 1.5),
            new Vector2(22, 1.5),
            new Vector2(22, 3),
            new Vector2(0, 3)
        )),
        new Polygon(11, -3, List.of(
            new Vector2(0, 6.5),
            new Vector2(7, 1.5),
            new Vector2(7, 3),
            new Vector2(0, 8)
        )),
        new Polygon(-10, 0, List.of(
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 3.5),
            new Vector2(0, 3.5)
        )),
        new Polygon(8, 0, List.of(
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 3.5),
            new Vector2(0, 3.5)
        )),
        new Polygon(-10, 3.5, List.of(
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 5.5),
            new Vector2(0, 5.5)
        )),
        new Polygon(8, 3.5, List.of(
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 5.5),
            new Vector2(0, 5.5)
        )),
        new Polygon(-10, 9, List.of(
            new Vector2(0, 0),
            new Vector2(20, 0),
            new Vector2(20, 1.5),
            new Vector2(0, 1.5)
        )),
        new Polygon(-12, 10.5, List.of(
            new Vector2(0, 0),
            new Vector2(24, 0),
            new Vector2(24, 1.5),
            new Vector2(0, 1.5)
        )),
        new Polygon(-6, 2.5, List.of(
            new Vector2(0, 1),
            new Vector2(3, 3),
            new Vector2(3, 4),
            new Vector2(0, 2)
        )),
        new Polygon(-3, 4.5, List.of(
            new Vector2(0, 1),
            new Vector2(6, 1),
            new Vector2(6, 2),
            new Vector2(0, 2)
        )),
        new Polygon(3, 2.5, List.of(
            new Vector2(0, 3),
            new Vector2(3, 1),
            new Vector2(3, 2),
            new Vector2(0, 4)
        )),
        new Polygon(-7, 12, List.of(
            new Vector2(0, 0),
            new Vector2(3.5, 0),
            new Vector2(3.5, 1.5),
            new Vector2(0, 1.5)
        )),
        new Polygon(3.5, 12, List.of(
            new Vector2(0, 0),
            new Vector2(3.5, 0),
            new Vector2(3.5, 1.5),
            new Vector2(0, 1.5)
        )),
        new Polygon(-6.8, 13.5, List.of(
            new Vector2(0, 0),
            new Vector2(0.9, 0),
            new Vector2(0.9, 1),
            new Vector2(0, 1)
        )),
        new Polygon(-5.6, 13.5, List.of(
            new Vector2(0, 0),
            new Vector2(0.9, 0),
            new Vector2(0.9, 1),
            new Vector2(0, 1)
        )),
        new Polygon(-4.4, 13.5, List.of(
            new Vector2(0, 0),
            new Vector2(0.9, 0),
            new Vector2(0.9, 1),
            new Vector2(0, 1)
        )),
        new Polygon(3.5, 13.5, List.of(
            new Vector2(0, 0),
            new Vector2(0.9, 0),
            new Vector2(0.9, 1),
            new Vector2(0, 1)
        )),
        new Polygon(4.7, 13.5, List.of(
            new Vector2(0, 0),
            new Vector2(0.9, 0),
            new Vector2(0.9, 1),
            new Vector2(0, 1)
        )),
        new Polygon(5.9, 13.5, List.of(
            new Vector2(0, 0),
            new Vector2(0.9, 0),
            new Vector2(0.9, 1),
            new Vector2(0, 1)
        )),
        new Polygon(-11, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 6),
            new Vector2(0, 6)
        )),
        new Polygon(-7.8, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 6),
            new Vector2(0, 6)
        )),
        new Polygon(-10.2, 12, List.of(
            new Vector2(0, 0),
            new Vector2(2.4, 0),
            new Vector2(2.4, 3.7),
            new Vector2(0, 3.7)
        )),
        new Polygon(-10.2, 17.3, List.of(
            new Vector2(0, 0),
            new Vector2(2.4, 0),
            new Vector2(2.4, 0.7),
            new Vector2(0, 0.7)
        )),
        new Polygon(-11, 18, List.of(
            new Vector2(0, 0),
            new Vector2(4, 0),
            new Vector2(4, 0.5),
            new Vector2(0, 0.5)
        )),
        new Polygon(-11, 18.5, List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1.2),
            new Vector2(0, 1.2)
        )),
        new Polygon(-9.5, 18.5, List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1.2),
            new Vector2(0, 1.2)
        )),
        new Polygon(-8, 18.5, List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1.2),
            new Vector2(0, 1.2)
        )),
        new Polygon(7, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 6),
            new Vector2(0, 6)
        )),
        new Polygon(10.2, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 6),
            new Vector2(0, 6)
        )),
        new Polygon(7.8, 12, List.of(
            new Vector2(0, 0),
            new Vector2(2.4, 0),
            new Vector2(2.4, 3.7),
            new Vector2(0, 3.7)
        )),
        new Polygon(7.8, 17.3, List.of(
            new Vector2(0, 0),
            new Vector2(2.4, 0),
            new Vector2(2.4, 0.7),
            new Vector2(0, 0.7)
        )),
        new Polygon(7, 18, List.of(
            new Vector2(0, 0),
            new Vector2(4, 0),
            new Vector2(4, 0.5),
            new Vector2(0, 0.5)
        )),
        new Polygon(7, 18.5, List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1.2),
            new Vector2(0, 1.2)
        )),
        new Polygon(8.5, 18.5, List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1.2),
            new Vector2(0, 1.2)
        )),
        new Polygon(10, 18.5, List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1.2),
            new Vector2(0, 1.2)
        )),
        new Polygon(-3.5, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 7),
            new Vector2(0, 7)
        )),
        new Polygon(2.7, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 7),
            new Vector2(0, 7)
        )),
        new Polygon(-0.4, 12, List.of(
            new Vector2(0, 0),
            new Vector2(0.8, 0),
            new Vector2(0.8, 7),
            new Vector2(0, 7)
        )),
        new Polygon(-2.7, 12, List.of(
            new Vector2(0, 0),
            new Vector2(2.3, 0),
            new Vector2(2.3, 2.5),
            new Vector2(0, 2.5)
        )),
        new Polygon(-2.7, 17, List.of(
            new Vector2(0, 0),
            new Vector2(2.3, 0),
            new Vector2(2.3, 2),
            new Vector2(0, 2)
        )),
        new Polygon(0.4, 12, List.of(
            new Vector2(0, 0),
            new Vector2(2.3, 0),
            new Vector2(2.3, 2.5),
            new Vector2(0, 2.5)
        )),
        new Polygon(0.4, 17, List.of(
            new Vector2(0, 0),
            new Vector2(2.3, 0),
            new Vector2(2.3, 2),
            new Vector2(0, 2)
        )),
        new Polygon(-4.5, 19, List.of(
            new Vector2(0, 0),
            new Vector2(9, 0),
            new Vector2(4.5, 4)
        )),
        new Polygon(-0.08, 23, List.of(
            new Vector2(0, 0),
            new Vector2(0.16, 0),
            new Vector2(0.16, 1.8),
            new Vector2(0, 1.8)
        )),
        new Polygon(0.08, 24, List.of(
            new Vector2(0, 0),
            new Vector2(1.8, 0.5),
            new Vector2(0, 1.3)
        )),
        new Circle(-9, 19.9, 0.4),
        new Circle(9, 19.9, 0.4),
        new Circle(0, 23.3, 0.4)
        ));
    }
}
