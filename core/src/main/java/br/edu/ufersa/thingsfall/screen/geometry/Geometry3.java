
package br.edu.ufersa.thingsfall.screen.geometry;

import br.edu.ufersa.thingsfall.common.DynamicArray;
import br.edu.ufersa.thingsfall.math.Vector2;
import com.badlogic.gdx.graphics.Color;

public final class Geometry3 extends GeometryBase {

    private static final double SCALE = 20.0;
    private static final double ORIGIN_X = 378.0;
    private static final double ORIGIN_Y = 600.0;
    private static final double LINE_WIDTH = 5.0;

    public Geometry3() {
        this(Color.valueOf("a0a0a0"));
    }

    public Geometry3(Color color) {
        super(color, createObjects());
    }

    private static DynamicArray<GeometryObject> createObjects() {
        DynamicArray<GeometryObject> objects = DynamicArray.of();

        // Left building
        frame(objects, 70, 156, 150, 444);

        // Middle building
        frame(objects, 277, 133, 167, 467);

        // Right building
        frame(objects, 498, 199, 188, 401);

        // Left building windows
        double[][] leftWindows = {
            {90, 183, 40, 41},
            {149, 182, 50, 43},

            {89, 256, 47, 43},
            {154, 256, 40, 43},

            {90, 333, 41, 42},
            {149, 332, 50, 44},

            {89, 407, 46, 42},
            {154, 407, 39, 42},

            {91, 477, 40, 41},
            {150, 476, 50, 42},

            {90, 550, 42, 44},
            {155, 550, 43, 44}
        };

        for (double[] window : leftWindows) {
            frame(objects, window[0], window[1], window[2], window[3]);
        }

        // Middle building windows
        double[][] middleWindows = {
            {300, 162, 50, 61},
            {366, 279, 47, 70},
            {304, 426, 51, 80}
        };

        for (double[] window : middleWindows) {
            frame(objects, window[0], window[1], window[2], window[3]);
        }

        // Right building windows
        double[][] rightWindows = {
            {529, 239, 48, 50},
            {596, 238, 59, 52},

            {528, 323, 56, 53},
            {602, 324, 46, 51},

            {529, 411, 48, 50},
            {596, 410, 59, 52},

            {528, 495, 56, 52},
            {602, 496, 46, 51}
        };

        for (double[] window : rightWindows) {
            frame(objects, window[0], window[1], window[2], window[3]);
        }

        return objects;
    }

    /**
     * Creates a hollow rectangular outline using four polygons.
     *
     * Coordinates are given in image-space pixels, with the
     * origin at the center-bottom of the reference image.
     */
    private static void frame(
        DynamicArray<GeometryObject> objects,
        double x,
        double y,
        double width,
        double height
    ) {
        double thickness = LINE_WIDTH / SCALE;

        double px = (x - ORIGIN_X) / SCALE;
        double py = (ORIGIN_Y - y - height) / SCALE;

        double w = width / SCALE;
        double h = height / SCALE;

        // Bottom edge
        objects.add(rectangle(px, py, w, thickness));

        // Top edge
        objects.add(rectangle(px, py + h - thickness, w, thickness));

        // Left edge
        objects.add(rectangle(px, py + thickness, thickness, h - 2 * thickness));

        // Right edge
        objects.add(rectangle(
            px + w - thickness,
            py + thickness,
            thickness,
            h - 2 * thickness
        ));
    }

    /**
     * Creates a rectangular polygon.
     */
    private static Polygon rectangle(
        double x,
        double y,
        double width,
        double height
    ) {
        return new Polygon(x, y, DynamicArray.of(
            new Vector2(0, 0),
            new Vector2(width, 0),
            new Vector2(width, height),
            new Vector2(0, height)
        ));
    }
}
