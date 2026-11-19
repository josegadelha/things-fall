package br.edu.ufersa.thingsfall.screen.level;

import br.edu.ufersa.thingsfall.graphic.ConstraintRenderer;
import br.edu.ufersa.thingsfall.graphic.ConfettiRenderer;
import br.edu.ufersa.thingsfall.graphic.ExtendedShapeRenderer;
import br.edu.ufersa.thingsfall.graphic.GridRenderer;
import br.edu.ufersa.thingsfall.graphic.ImpulseArrowRenderer;
import br.edu.ufersa.thingsfall.graphic.PhysicsShapeRenderer;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.screen.LevelProgress;
import br.edu.ufersa.thingsfall.screen.LevelSelectionScreen;
import br.edu.ufersa.thingsfall.screen.geometry.VictoryArea;
import br.edu.ufersa.thingsfall.ui.ScrollablePanel;
import br.edu.ufersa.thingsfall.ui.SimplePhysicsSkin;
import br.edu.ufersa.thingsfall.ui.SimulationButtons;
import br.edu.ufersa.thingsfall.ui.SimulationHistory;
import br.edu.ufersa.thingsfall.ui.SimulationInputController;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class LevelScreenBase extends ScreenAdapter {
    private static final float WORLD_UNITS_PER_PIXEL = 0.01f;
    private static final int PANEL_WIDTH = 350;
    private static final int PANEL_GAP = 24;
    private static final Color BACKGROUND_COLOR = Color.valueOf("242424");
    private static final Color PANEL_COLOR = Color.valueOf("2d2d2d");
    private static final Color SIMULATION_COLOR = Color.valueOf("fefefe");
    private final int levelIndex;
    private final Viewport panelViewport;
    private final ScreenViewport simulationViewport;
    private final Camera panelCamera;
    private final OrthographicCamera simulationCamera;

    private final ExtendedShapeRenderer extendedShapeRenderer;
    private final GridRenderer gridRenderer;
    private final ImpulseArrowRenderer impulseArrowRenderer;
    private final ConstraintRenderer constraintRenderer;
    private final PhysicsShapeRenderer physicsShapeRenderer;
    private final ConfettiRenderer confettiRenderer;
    protected final PhysicsWorld physicsWorld;

    private final Stage panelStage;
    private final InputMultiplexer inputMultiplexer;
    protected final SimulationInputController simulationInputController;
    private final SimulationHistory simulationHistory;
    protected final Skin skin;
    protected final Game game;
    protected final TextButton squareButton;
    protected final TextButton circleButton;
    protected final TextButton followObjectButton;
    protected final TextButton resetCameraButton;
    private final TextButton undoButton;
    private final TextButton redoButton;
    private final Table shapeSizeControl;
    protected final Table shapeOptionsTable;
    protected final Table physicsOptionsTable;
    protected final Table cameraOptionsTable;
    protected final Table navigationOptionsTable;
    private final TextButton backToMenuButton;

    private TextButton[] toolButtons;
    private VictoryArea victoryArea;
    private boolean winConditionMet;
    private boolean simulationStarted;

    protected LevelScreenBase(Game game, int levelIndex) {
        this(game, levelIndex, new SimulationHistory());
    }

    protected LevelScreenBase(Game game, int levelIndex, SimulationHistory simulationHistory) {
        this.game = game;
        this.levelIndex = levelIndex;
        this.simulationHistory = simulationHistory;

        panelCamera = new OrthographicCamera();
        simulationCamera = new OrthographicCamera();
        simulationCamera.zoom = 3f;
        panelViewport = new ScreenViewport(panelCamera);
        simulationViewport = new ScreenViewport(simulationCamera);
        simulationViewport.setUnitsPerPixel(WORLD_UNITS_PER_PIXEL);

        extendedShapeRenderer = new ExtendedShapeRenderer();
        gridRenderer = new GridRenderer(1f, true);
        impulseArrowRenderer = new ImpulseArrowRenderer();
        constraintRenderer = new ConstraintRenderer();
        physicsShapeRenderer = new PhysicsShapeRenderer();
        confettiRenderer = new ConfettiRenderer();
        physicsWorld = new PhysicsWorld();

        skin = SimplePhysicsSkin.create();
        shapeOptionsTable = createCategory(skin, "Object Settings");
        physicsOptionsTable = createCategory(skin, "Physics Settings");
        cameraOptionsTable = createCategory(skin, "Camera Settings");
        navigationOptionsTable = createCategory(skin, "Navigation");
        panelStage = new Stage(panelViewport);
        simulationInputController = new SimulationInputController(
            simulationViewport,
            simulationCamera,
            physicsWorld,
            simulationHistory,
            WORLD_UNITS_PER_PIXEL
        );
        squareButton = SimulationButtons.createSquareButton(skin, simulationInputController);
        circleButton = SimulationButtons.createCircleButton(skin, simulationInputController);
        followObjectButton = SimulationButtons.createFollowObjectButton(skin, simulationInputController);
        resetCameraButton = SimulationButtons.createResetCameraButton(skin, simulationInputController);
        undoButton = SimulationButtons.createUndoButton(skin, simulationHistory, physicsWorld);
        redoButton = SimulationButtons.createRedoButton(skin, simulationHistory, physicsWorld);
        backToMenuButton = SimulationButtons.createBackToLevelSelectionButton(game, this, skin);

        shapeSizeControl = SimulationButtons.createShapeSizeControl(
            skin,
            squareButton,
            circleButton,
            simulationInputController
        );
        shapeOptionsTable.add(shapeSizeControl).width(300).height(50);
        shapeOptionsTable.row().padTop(12);
        shapeOptionsTable.add(squareButton).width(300).height(50);
        shapeOptionsTable.row().padTop(12);
        shapeOptionsTable.add(circleButton).width(300).height(50);
        cameraOptionsTable.add(followObjectButton).width(300).height(50);
        cameraOptionsTable.row().padTop(12);
        cameraOptionsTable.add(resetCameraButton).width(300).height(50);
        toolButtons = new TextButton[] { squareButton, circleButton };

        Table panelTable = new Table(skin);
        panelTable.top();
        Table panelLayout = new Table(skin);
        panelLayout.setFillParent(true);
        panelStage.addActor(panelLayout);

        ScrollablePanel settingsPanel = new ScrollablePanel(
            panelStage,
            panelViewport,
            panelTable,
            skin,
            simulationInputController.getInputProcessor()
        );
        panelLayout.add(settingsPanel.getScrollPane())
            .width(PANEL_WIDTH)
            .expandY()
            .fillY()
            .padTop(PANEL_GAP)
            .padBottom(PANEL_GAP);

        panelTable.add(navigationOptionsTable).width(300);
        panelTable.row().padTop(12);
        panelTable.add(shapeOptionsTable).width(300);
        panelTable.row().padTop(12);
        panelTable.add(physicsOptionsTable).width(300);
        panelTable.row().padTop(12);
        panelTable.add(cameraOptionsTable).width(300);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(settingsPanel.getInputProcessor());
        inputMultiplexer.addProcessor(panelStage);
        inputMultiplexer.addProcessor(simulationInputController.getInputProcessor());
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private static Table createCategory(Skin skin, String title) {
        Table category = new Table(skin);
        category.add(new Label(title, skin)).center();
        category.row().padTop(12);
        return category;
    }

    protected final void configureTools(TextButton... physicsTools) {
        toolButtons = new TextButton[physicsTools.length + 2];
        toolButtons[0] = squareButton;
        toolButtons[1] = circleButton;
        System.arraycopy(physicsTools, 0, toolButtons, 2, physicsTools.length);
        SimulationButtons.configureSimulationToolGroup(followObjectButton, toolButtons);
    }

    protected final void configureNavigation(TextButton startButton) {
        if (startButton != null) {
            navigationOptionsTable.add(startButton).width(300).height(50);
            navigationOptionsTable.row().padTop(12);
        }

        Table historyButtons = new Table(skin);
        historyButtons.add(undoButton).expandX().fillX().height(50).padRight(6);
        historyButtons.add(redoButton).expandX().fillX().height(50).padLeft(6);
        navigationOptionsTable.add(historyButtons).width(300);
        navigationOptionsTable.row().padTop(12);
        navigationOptionsTable.add(backToMenuButton).width(300).height(50);
    }

    private void updateButtons() {
        if (!simulationStarted) {
            SimulationButtons.updateDisabledButtons(
                new TextButton[] { followObjectButton },
                toolButtons,
                simulationInputController.isWaitingForFollowTarget()
            );
        }
        SimulationButtons.updateFollowObjectButtonLabel(followObjectButton, simulationInputController);
        undoButton.setDisabled(simulationStarted || !simulationHistory.canUndo());
        redoButton.setDisabled(simulationStarted || !simulationHistory.canRedo());
    }

    protected final void onSimulationStarted() {
        simulationStarted = true;
        setControlsDisabled(shapeOptionsTable);
        shapeSizeControl.setColor(1f, 1f, 1f, 0.4f);
        setControlsDisabled(physicsOptionsTable);
        undoButton.setDisabled(true);
        redoButton.setDisabled(true);
    }

    protected final void restartLevel(LevelScreenBase newScreen) {
        newScreen.simulationCamera.position.set(simulationCamera.position);
        newScreen.simulationCamera.zoom = simulationCamera.zoom;
        newScreen.simulationCamera.update();
        game.setScreen(newScreen);
        dispose();
    }

    private static void setControlsDisabled(Group group) {
        for (int i = 0; i < group.getChildren().size; i++) {
            com.badlogic.gdx.scenes.scene2d.Actor actor = group.getChildren().get(i);
            if (actor instanceof Button) {
                ((Button) actor).setDisabled(true);
            } else if (actor instanceof TextField) {
                ((TextField) actor).setDisabled(true);
            }
            if (actor instanceof Group) {
                setControlsDisabled((Group) actor);
            }
        }
    }

    protected final void addVictoryArea(VictoryArea victoryArea) {
        this.victoryArea = victoryArea;
        physicsWorld.addGeometry(victoryArea.spawnObjects());
    }

    @Override
    public void render(float delta) {
        float simulationWidth = simulationViewport.getWorldWidth() * simulationCamera.zoom;
        float simulationHeight = simulationViewport.getWorldHeight() * simulationCamera.zoom;

        ScreenUtils.clear(BACKGROUND_COLOR);

        if (!simulationInputController.isApplyingImpulse()) {
            physicsWorld.step(delta);
        }
        simulationInputController.updateCameraFollow();
        updateButtons();

        panelViewport.apply();
        extendedShapeRenderer.setProjectionMatrix(panelCamera.combined);
        extendedShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        extendedShapeRenderer.setColor(PANEL_COLOR);
        extendedShapeRenderer.roundedRect(
            0,
            0,
            panelViewport.getWorldWidth(),
            panelViewport.getWorldHeight(),
            10
        );
        extendedShapeRenderer.end();
        panelStage.act();
        panelStage.draw();

        simulationViewport.apply();
        extendedShapeRenderer.setProjectionMatrix(simulationCamera.combined);
        extendedShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        extendedShapeRenderer.setColor(SIMULATION_COLOR);
        extendedShapeRenderer.roundedRect(
            simulationCamera.position.x - simulationWidth / 2f,
            simulationCamera.position.y - simulationHeight / 2f,
            simulationWidth,
            simulationHeight,
            0.1f * simulationCamera.zoom
        );
        extendedShapeRenderer.end();

        gridRenderer.render(simulationCamera, simulationWidth, simulationHeight);
        physicsShapeRenderer.render(simulationCamera, physicsWorld);
        constraintRenderer.render(
            simulationCamera,
            physicsWorld,
            simulationInputController.getRevoluteDragStart(),
            simulationInputController.getRevoluteDragEnd()
        );
        impulseArrowRenderer.render(
            simulationCamera,
            simulationInputController.getImpulseStart(),
            simulationInputController.getImpulseEnd()
        );

        winConditionMet();
        if (winConditionMet && confettiRenderer.renderWinCelebration(delta)) {
            game.setScreen(new LevelSelectionScreen(game));
            dispose();
        }
    }

    protected final void winConditionMet() {
        if (!winConditionMet && victoryArea != null && victoryArea.isReached(physicsWorld)) {
            winConditionMet = true;
            LevelProgress.markCompleted(levelIndex);
            confettiRenderer.spawn();
        }
    }

    @Override
    public void resize(int width, int height) {
        int availableHeight = height - 2 * PANEL_GAP;
        int availableWidth = width - 3 * PANEL_GAP;
        int simulationWidth = availableWidth - PANEL_WIDTH;

        panelViewport.update(PANEL_WIDTH, availableHeight, true);
        panelViewport.setScreenBounds(PANEL_GAP, PANEL_GAP, PANEL_WIDTH, availableHeight);

        simulationViewport.update(simulationWidth, availableHeight, false);
        simulationViewport.setScreenBounds(2 * PANEL_GAP + PANEL_WIDTH, PANEL_GAP, simulationWidth, availableHeight);
        simulationCamera.update();
        confettiRenderer.resize(
            width,
            height,
            2 * PANEL_GAP + PANEL_WIDTH,
            PANEL_GAP,
            simulationWidth,
            availableHeight
        );
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputMultiplexer) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        extendedShapeRenderer.dispose();
        gridRenderer.dispose();
        impulseArrowRenderer.dispose();
        constraintRenderer.dispose();
        physicsShapeRenderer.dispose();
        confettiRenderer.dispose();
        panelStage.dispose();
        skin.dispose();
    }
}
