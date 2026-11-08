package br.edu.ufersa.thingsfall.screen;

import br.edu.ufersa.thingsfall.graphic.GridRenderer;
import br.edu.ufersa.thingsfall.screen.level.Level1Screen;
import br.edu.ufersa.thingsfall.screen.level.Level2Screen;
import br.edu.ufersa.thingsfall.screen.level.LevelSandboxScreen;
import br.edu.ufersa.thingsfall.ui.SimplePhysicsSkin;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.function.Supplier;

public class LevelSelectionScreen extends ScreenAdapter {
    private static final float WORLD_UNITS_PER_PIXEL = 0.02f;
    private static final String TITLE_TEXT = "Select Level";
    private static final int TITLE_FONT_SIZE = 72;
    private static final float TILE_SIZE = 150f;
    private static final float TILE_SPACING = 25f;
    private static final Color BACKGROUND_COLOR = Color.valueOf("ffffff");
    private static final Color TITLE_COLOR = Color.valueOf("3a3a3a");

    private final Game game;
    private final OrthographicCamera worldCamera;
    private final ScreenViewport worldViewport;
    private final GridRenderer gridRenderer;

    private final Stage uiStage;
    private final Skin skin;
    private final BitmapFont titleFont;

    // Slots 0..4 are levels 1..5 (same indices as LevelProgress), slot 5 is the sandbox.
    private final Supplier<Screen>[] levels = createLevelTable();

    @SuppressWarnings("unchecked")
    private Supplier<Screen>[] createLevelTable() {
        return new Supplier[] {
            () -> new Level1Screen(game),
            () -> new Level2Screen(game),
            null,
            null,
            null,
            () -> new LevelSandboxScreen(game)
        };
    }

    public LevelSelectionScreen(Game game) {
        this.game = game;

        worldCamera = new OrthographicCamera();
        worldViewport = new ScreenViewport(worldCamera);
        worldViewport.setUnitsPerPixel(WORLD_UNITS_PER_PIXEL);

        gridRenderer = new GridRenderer(1f, false);

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

    private boolean isSlotUnlocked(int slot) {
        return slot < LevelProgress.LEVEL_COUNT ? LevelProgress.isUnlocked(slot) : LevelProgress.isSandboxUnlocked();
    }

    private void createMenu() {
        Table menu = new Table();
        menu.setFillParent(true);
        uiStage.addActor(menu);

        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.font = titleFont;
        Label title = new Label(TITLE_TEXT, titleStyle);

        Table levelTable = new Table();
        for (int i = 0; i < levels.length; i++) {
            boolean sandbox = i >= LevelProgress.LEVEL_COUNT;
            TextButton tile = new TextButton(sandbox ? "Sandbox" : String.valueOf(i + 1), skin);
            if (sandbox) {
                tile.getLabel().setFontScale(0.6f);
            }

            final Supplier<Screen> factory = levels[i];
            if (factory == null || !isSlotUnlocked(i)) {
                tile.setDisabled(true);
                tile.getColor().a = 0.45f;
            } else {
                tile.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        game.setScreen(factory.get());
                        dispose();
                    }
                });
            }
            levelTable.add(tile).size(TILE_SIZE).pad(TILE_SPACING / 2f);
        }

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new TitleScreen(game));
                dispose();
            }
        });

        menu.add(title).padTop(0).padBottom(25).row();
        menu.add(levelTable).padBottom(50).row();
        menu.add(backButton).size(200, 64);
    }

    @Override
    public void render(float delta) {
        float frameTime = Math.min(delta, 0.1f);

        ScreenUtils.clear(BACKGROUND_COLOR);
        worldViewport.apply();
        gridRenderer.render(worldCamera, worldViewport.getWorldWidth(), worldViewport.getWorldHeight());

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
        uiStage.dispose();
        titleFont.dispose();
        skin.dispose();
    }
}
