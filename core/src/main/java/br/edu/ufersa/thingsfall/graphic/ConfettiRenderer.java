package br.edu.ufersa.thingsfall.graphic;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ConfettiRenderer {
    private static final float CELEBRATION_DURATION = 2.5f;
    private static final int CONFETTI_COUNT = 120;

    private final PhysicsShapeRenderer shapeRenderer = new PhysicsShapeRenderer();
    private final PhysicsWorld confettiWorld = new PhysicsWorld();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ScreenViewport viewport = new ScreenViewport(camera);
    private float celebrationTime;

    public ConfettiRenderer() {
        confettiWorld.collisionsEnabled = false;
        viewport.setUnitsPerPixel(0.01f);
    }

    public void resize(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        viewport.update(width, height, false);
        viewport.setScreenBounds(x, y, width, height);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0f);
        camera.zoom = 1f;
        camera.update();
    }

    public void spawn() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float leftEdge = camera.position.x - worldWidth / 2f;
        float rightEdge = camera.position.x + worldWidth / 2f;
        float bottomEdge = camera.position.y - worldHeight / 2f;

        for (int confettiIndex = 0; confettiIndex < CONFETTI_COUNT; confettiIndex++) {
            boolean spawnsFromLeft = confettiIndex % 2 == 0;
            double size = MathUtils.random(0.12f, 0.28f);

            // outside the screen on the side, and pushed down by the same amount again.
            float horizontalOffset = MathUtils.random(0.2f, 0.6f);
            float verticalOffset = MathUtils.random(0.2f, 0.6f) + MathUtils.random(0.2f, 0.6f);

            double positionX = spawnsFromLeft ? leftEdge - horizontalOffset : rightEdge + horizontalOffset;
            double positionY = bottomEdge - verticalOffset;
            confettiWorld.addSquare(positionX, positionY, size, false);

            // diagonal impulse.
            float velocityX = MathUtils.random(2f, 6f) * (spawnsFromLeft ? 1f : -1f);
            float velocityY = MathUtils.random(8f, 14f);

            var confetti = confettiWorld.objects.get(confettiWorld.objects.size() - 1);
            confetti.velocity = new Vector2(velocityX, velocityY);
            confetti.angularVelocity = MathUtils.random(-8f, 8f);
        }
    }

    public boolean renderWinCelebration(float delta) {
        viewport.apply();
        confettiWorld.step(delta);
        shapeRenderer.render(camera, confettiWorld);
        celebrationTime += delta;
        return celebrationTime >= CELEBRATION_DURATION;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
