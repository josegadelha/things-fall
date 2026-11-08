package br.edu.ufersa.thingsfall.graphic;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.physics.constraint.Constraint;
import br.edu.ufersa.thingsfall.physics.constraint.RevoluteConstraint;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ConstraintRenderer {
    private static final int CIRCLE_SEGMENTS = 16;
    private static final Color JOINT_COLOR = Color.valueOf("686868");
    private static final Color PREVIEW_COLOR = Color.valueOf("a0a0a0");
    private static final float ANCHOR_RADIUS = 0.0125f;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public void render(
        Camera camera,
        PhysicsWorld world,
        Vector2 pendingAnchorStart,
        Vector2 pendingAnchorEnd
    ) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(JOINT_COLOR);
        try {
            for (Constraint constraint : world.constraints) {
                if (constraint instanceof RevoluteConstraint) {
                    drawConstraint((RevoluteConstraint) constraint);
                }
            }

            if (pendingAnchorStart != null && pendingAnchorEnd != null) {
                shapeRenderer.setColor(PREVIEW_COLOR);
                shapeRenderer.line(
                    (float) pendingAnchorStart.x,
                    (float) pendingAnchorStart.y,
                    (float) pendingAnchorEnd.x,
                    (float) pendingAnchorEnd.y
                );
            }
        } finally {
            shapeRenderer.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(JOINT_COLOR);
        try {
            for (Constraint constraint : world.constraints) {
                if (constraint instanceof RevoluteConstraint) {
                    RevoluteConstraint revolute = (RevoluteConstraint) constraint;
                    drawAnchor(revolute.bodyA.localToWorld(revolute.localAnchorOnBodyA), ANCHOR_RADIUS);
                    drawAnchor(revolute.bodyB.localToWorld(revolute.localAnchorOnBodyB), ANCHOR_RADIUS);
                }
            }

            if (pendingAnchorStart != null && pendingAnchorEnd != null) {
                shapeRenderer.setColor(PREVIEW_COLOR);
                drawAnchor(pendingAnchorStart, ANCHOR_RADIUS);
                drawAnchor(pendingAnchorEnd, ANCHOR_RADIUS);
            }
        } finally {
            shapeRenderer.end();
        }
    }

    private void drawConstraint(RevoluteConstraint constraint) {
        Vector2 anchorOnBodyA = constraint.bodyA.localToWorld(constraint.localAnchorOnBodyA);
        Vector2 anchorOnBodyB = constraint.bodyB.localToWorld(constraint.localAnchorOnBodyB);
        shapeRenderer.line(
            (float) anchorOnBodyA.x,
            (float) anchorOnBodyA.y,
            (float) anchorOnBodyB.x,
            (float) anchorOnBodyB.y
        );
    }

    private void drawAnchor(Vector2 position, float radius) {
        shapeRenderer.circle(
            (float) position.x,
            (float) position.y,
            radius,
            CIRCLE_SEGMENTS
        );
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
