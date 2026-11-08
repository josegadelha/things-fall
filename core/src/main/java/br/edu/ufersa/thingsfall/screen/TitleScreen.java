package br.edu.ufersa.thingsfall.screen;

import br.edu.ufersa.thingsfall.graphic.GridRenderer;
import br.edu.ufersa.thingsfall.graphic.PhysicsShapeRenderer;
import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.ui.SimplePhysicsSkin;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TitleScreen extends ScreenAdapter {
    private static final float WORLD_UNITS_PER_PIXEL = 0.02f;
    private static final float CUBE_SPAWN_INTERVAL = 0.45f;
    private static final String TITLE_TEXT = "Things Fall";
    private static final int TITLE_FONT_SIZE = 96;
    private static final Color BACKGROUND_COLOR = Color.valueOf("ffffff");
    private static final Color TITLE_COLOR = Color.valueOf("3a3a3a");

    private final Game game;
    private final OrthographicCamera worldCamera;
    private final ScreenViewport worldViewport;
    private final GridRenderer gridRenderer;
    private final PhysicsShapeRenderer physicsShapeRenderer;
    private final PhysicsWorld physicsWorld;

    private final Stage uiStage;
    private final Skin skin;
    private final BitmapFont titleFont;
    private float spawnTimer;

    public TitleScreen(Game game) {
        this.game = game;

        worldCamera = new OrthographicCamera();
        worldViewport = new ScreenViewport(worldCamera);
        worldViewport.setUnitsPerPixel(WORLD_UNITS_PER_PIXEL);

        gridRenderer = new GridRenderer(1f, false);
        physicsShapeRenderer = new PhysicsShapeRenderer();
        physicsWorld = new PhysicsWorld();

        skin = SimplePhysicsSkin.create();
        titleFont = createTitleFont();
        uiStage = new Stage(new ScreenViewport());
        worldViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        createMenu();
    }

    private BitmapFont createTitleFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/FiraCode-Bold.ttf"));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = TITLE_FONT_SIZE;
            parameter.color = TITLE_COLOR;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }
    }

    private void createMenu() {
        Table menu = new Table();
        menu.setFillParent(true);
        uiStage.addActor(menu);

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.font = titleFont;
        HorizontalGroup titleRow = new HorizontalGroup();
        titleRow.space(4);
        for (char letter : TITLE_TEXT.toCharArray()) {
            titleRow.addActor(new Label(String.valueOf(letter), titleStyle));
        }

        TextButton playButton = new TextButton("Play", skin);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playButton.setTouchable(Touchable.disabled);
                float lastStart = 0f;
                float lettersEnd = 0f;
                for (Actor letter : titleRow.getChildren()) {
                    float delay = MathUtils.random(0f, 0.8f);
                    float fallTime = MathUtils.random(0.5f, 1.1f);
                    lastStart = Math.max(lastStart, delay);
                    lettersEnd = Math.max(lettersEnd, delay + fallTime);
                    float distance = uiStage.getViewport().getWorldHeight() + letter.getHeight() + 40f;
                    letter.addAction(Actions.sequence(
                        Actions.delay(delay),
                        Actions.moveBy(0, -distance, fallTime, Interpolation.pow2In)
                    ));
                }
                float buttonFallTime = 0.5f;
                float buttonDelay = Math.max(lastStart + 0.35f, lettersEnd - buttonFallTime + 0.35f);
                float buttonDistance = uiStage.getViewport().getWorldHeight() + playButton.getHeight() + 40f;
                playButton.addAction(Actions.sequence(
                    Actions.delay(buttonDelay),
                    Actions.moveBy(0, -buttonDistance, buttonFallTime, Interpolation.pow2In),
                    Actions.delay(0.25f),
                    Actions.run(() -> {
                        game.setScreen(new LevelSelectionScreen(game));
                        dispose();
                    })
                ));
            }
        });
        menu.add(titleRow).padTop(36).padBottom(36).row();
        menu.add(playButton).size(240, 76);
    }

    private void spawnCube(float x, float y) {
        float size = MathUtils.random(1f, 1.8f);
        physicsWorld.addSquare(x, y, size, false);
        PhysicsObject cube = physicsWorld.objects.getLast();
        cube.renderDetails = false;
        cube.angularVelocity = MathUtils.random(-0.5f, 0.5f);
        cube.velocity = new Vector2(MathUtils.random(-0.5f, 0.5f), MathUtils.random(-0.5f, 0.5f));
    }

    @Override
    public void render(float delta) {
        float frameTime = Math.min(delta, 0.1f);
        spawnTimer += frameTime;
        while (spawnTimer >= CUBE_SPAWN_INTERVAL) {
            spawnTimer -= CUBE_SPAWN_INTERVAL;
            spawnCube(
                MathUtils.random(-worldViewport.getWorldWidth() / 2f, worldViewport.getWorldWidth() / 2f),
                worldViewport.getWorldHeight() / 2f + MathUtils.random(0.5f, 2f)
            );
        }

        physicsWorld.step(frameTime);
        float minY = worldCamera.position.y - worldViewport.getWorldHeight() / 2f - 2f;
        physicsWorld.objects.removeIf(object -> object.position.y < minY);

        ScreenUtils.clear(BACKGROUND_COLOR);
        worldViewport.apply();
        gridRenderer.render(worldCamera, worldViewport.getWorldWidth(), worldViewport.getWorldHeight());
        physicsShapeRenderer.render(worldCamera, physicsWorld);

        uiStage.act(frameTime);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        worldCamera.update();
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == uiStage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        gridRenderer.dispose();
        physicsShapeRenderer.dispose();
        uiStage.dispose();
        titleFont.dispose();
        skin.dispose();
    }
}
