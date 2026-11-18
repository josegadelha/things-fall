package br.edu.ufersa.thingsfall.ui;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsWorld;
import br.edu.ufersa.thingsfall.screen.LevelSelectionScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

public class SimulationButtons {
    // Button creation is provided through static methods, instances are not needed.
    private SimulationButtons() {
    }

    // Creates a button that returns to the level selection screen and disposes of the current screen.
    public static TextButton createBackToLevelSelectionButton(Game game, ScreenAdapter currentScreen, Skin skin) {
        TextButton button = new TextButton("Back to menu", skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectionScreen(game));
                currentScreen.dispose();
            }
        });
        return button;
    }

    // Creates a toggle button that selects the square-creation tool.
    public static TextButton createSquareButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToolButton(
            "Create square",
            SimulationInputController.Tool.CREATE_SQUARE,
            skin,
            inputController
        );
    }

    // Creates a toggle button that selects the circle-creation tool.
    public static TextButton createCircleButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToolButton(
            "Create circle",
            SimulationInputController.Tool.CREATE_CIRCLE,
            skin,
            inputController
        );
    }

    public static TextButton createDragObjectButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToolButton(
            "Drag objects",
            SimulationInputController.Tool.DRAG_OBJECT,
            skin,
            inputController
        );
    }

    // Shows a prompt until a shape is selected, then displays that shape's size control.
    public static Table createShapeSizeControl(
        Skin skin,
        TextButton squareButton,
        TextButton circleButton,
        SimulationInputController inputController
    ) {
        Label title = new Label("Shape size", skin);
        Label.LabelStyle descriptionStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        descriptionStyle.fontColor = Color.valueOf("a0a0a0");
        Label description = new Label("Select a shape below", descriptionStyle);
        Table prompt = new Table(skin);
        prompt.setBackground(skin.get(TextButton.TextButtonStyle.class).up);
        prompt.add(title).center();
        prompt.row();
        prompt.add(description).center();
        Table circleRadiusControl = createSizeControl(
            "Radius",
            inputController.getCircleRadius(),
            skin,
            inputController::setCircleRadius,
            inputController::getCircleRadius
        );
        Table squareSideLengthControl = createSizeControl(
            "Side length",
            inputController.getSquareSideLength(),
            skin,
            inputController::setSquareSideLength,
            inputController::getSquareSideLength
        );

        Table shapeSizeControl = new Table(skin);

        ChangeListener selectionListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean circleSelected = circleButton.isChecked();
                boolean squareSelected = squareButton.isChecked();
                shapeSizeControl.clearChildren();
                if (circleSelected) {
                    shapeSizeControl.add(circleRadiusControl).width(300).fillX();
                } else if (squareSelected) {
                    shapeSizeControl.add(squareSideLengthControl).width(300).fillX();
                } else {
                    shapeSizeControl.add(prompt).width(300).height(50).fill();
                }
            }
        };
        squareButton.addListener(selectionListener);
        circleButton.addListener(selectionListener);
        selectionListener.changed(null, null);

        return shapeSizeControl;
    }

    private static Table createSizeControl(
        String label,
        double initialValue,
        Skin skin,
        java.util.function.DoubleConsumer valueSetter,
        java.util.function.DoubleSupplier valueGetter
    ) {
        TextField valueField = new FocusClearingTextField(formatSize(initialValue), skin);
        valueField.setTextFieldFilter(SimulationButtons::isValidSizeInput);
        valueField.setTextFieldListener((textField, character) -> updateSizeFromInput(textField, valueSetter));
        valueField.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    valueField.setText(formatSize(valueGetter.getAsDouble()));
                }
            }
        });

        TextButton increaseButton = createSizeAdjustmentButton(
            "\u2191",
            1.0,
            skin,
            valueField,
            valueSetter,
            valueGetter
        );
        TextButton decreaseButton = createSizeAdjustmentButton(
            "\u2193",
            -1.0,
            skin,
            valueField,
            valueSetter,
            valueGetter
        );

        Table arrowButtons = new Table(skin);
        arrowButtons.add(increaseButton).width(36).height(24);
        arrowButtons.row();
        arrowButtons.add(decreaseButton).width(36).height(24);

        Table sizeControl = new Table(skin);
        sizeControl.add(new Label(label, skin)).left().expandX();
        sizeControl.add(valueField).width(135).height(50);
        sizeControl.add(arrowButtons).width(36).height(50).padLeft(6);
        return sizeControl;
    }

    private static boolean isValidSizeInput(TextField textField, char character) {
        if (!Character.isDigit(character) && character != '.') {
            return false;
        }

        String currentText = textField.getText();
        int cursorPosition = Math.max(0, Math.min(textField.getCursorPosition(), currentText.length()));
        int selectionPosition = textField.getSelectionStart();
        int start = cursorPosition;
        int end = cursorPosition;
        if (selectionPosition >= 0 && selectionPosition != cursorPosition) {
            selectionPosition = Math.max(0, Math.min(selectionPosition, currentText.length()));
            start = Math.min(cursorPosition, selectionPosition);
            end = Math.max(cursorPosition, selectionPosition);
        }
        String candidate = currentText.substring(0, start) + character + currentText.substring(end);
        return candidate.matches("[0-9]*\\.?[0-9]{0,2}");
    }

    private static void updateSizeFromInput(
        TextField textField,
        java.util.function.DoubleConsumer valueSetter
    ) {
        try {
            double value = Double.parseDouble(textField.getText());
            if (Double.isFinite(value) && value > 0) {
                valueSetter.accept(roundSize(value));
            }
        } catch (NumberFormatException ignored) {
            // Intermediate input such as an empty field is not a complete value yet.
        }
    }

    private static TextButton createSizeAdjustmentButton(
        String label,
        double delta,
        Skin skin,
        TextField valueField,
        java.util.function.DoubleConsumer valueSetter,
        java.util.function.DoubleSupplier valueGetter
    ) {
        TextButton button = new TextButton(label, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateSizeField(valueField, valueSetter, valueGetter, delta);
            }
        });
        return button;
    }

    private static void updateSizeField(
        TextField valueField,
        java.util.function.DoubleConsumer valueSetter,
        java.util.function.DoubleSupplier valueGetter,
        double delta
    ) {
        double updatedValue = roundSize(Math.max(0.1, valueGetter.getAsDouble() + delta));
        valueSetter.accept(updatedValue);
        valueField.setText(formatSize(updatedValue));
    }

    private static double roundSize(double value) {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private static String formatSize(double value) {
        return BigDecimal.valueOf(roundSize(value))
            .stripTrailingZeros()
            .toPlainString();
    }

    private static final class FocusClearingTextField extends TextField {
        private final InputListener clickAwayListener = new InputListener() {
            @Override
            public boolean touchDown(
                InputEvent event,
                float x,
                float y,
                int pointer,
                int button
            ) {
                if (event.getTarget() != FocusClearingTextField.this) {
                    getStage().setKeyboardFocus(null);
                }
                return false;
            }
        };

        private Stage listenerStage;

        private FocusClearingTextField(String text, Skin skin) {
            super(text, skin);
        }

        @Override
        public void setStage(Stage stage) {
            if (listenerStage != null && listenerStage != stage) {
                listenerStage.getRoot().removeCaptureListener(clickAwayListener);
                if (listenerStage.getKeyboardFocus() == this) {
                    listenerStage.setKeyboardFocus(null);
                }
                listenerStage = null;
            }

            super.setStage(stage);
            if (stage != null && listenerStage == null) {
                listenerStage = stage;
                stage.getRoot().addCaptureListener(clickAwayListener);
            }
        }
    }

    // Creates a toggle button that enables or disables gravity in the physics world.
    public static TextButton createGravityButton(Skin skin, PhysicsWorld world) {
        return createToggleButton("Gravity", skin, true, enabled ->
            world.gravity = enabled ? new Vector2(0, -9.81) : new Vector2(0, 0)
        );
    }

    public static TextButton createStartRestartButton(
        Skin skin,
        PhysicsWorld world,
        Runnable onStarted,
        Runnable restart
    ) {
        TextButton button = new TextButton("Start", skin);
        button.addListener(new ChangeListener() {
            private boolean started;

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (started) {
                    restart.run();
                    return;
                }

                world.gravity = world.gravity.length() == 0
                    ? new Vector2(0, -9.81)
                    : new Vector2(0, 0);
                world.collisionsEnabled = !world.collisionsEnabled;
                world.reduceVelocityEnabled = !world.reduceVelocityEnabled;
                started = true;
                button.setText("Restart");
                onStarted.run();
            }
        });
        return button;
    }

    public static TextButton createVelocityReductionButton(Skin skin, PhysicsWorld world) {
        return createToggleButton(
            "Reduce velocity",
            skin,
            world.reduceVelocityEnabled,
            enabled -> world.reduceVelocityEnabled = enabled
        );
    }

    // Creates a toggle button that enables or disables collisions in the physics world.
    public static TextButton createCollisionButton(Skin skin, PhysicsWorld world) {
        return createToggleButton(
            "Collisions",
            skin,
            world.collisionsEnabled,
            enabled -> world.collisionsEnabled = enabled
        );
    }

    // Controls whether newly created physics objects are static.
    public static TextButton createStaticButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToggleButton("Is static", skin, false, inputController::setNewObjectStatic);
    }

    // Creates a toggle button that selects the impulse tool.
    public static TextButton createImpulseButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToolButton(
            "Apply impulse",
            SimulationInputController.Tool.APPLY_IMPULSE,
            skin,
            inputController
        );
    }

    public static TextButton createRevoluteConstraintButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToolButton(
            "Create revolute joint",
            SimulationInputController.Tool.CREATE_REVOLUTE_CONSTRAINT,
            skin,
            inputController
        );
    }

    public static TextButton createImpulseScalingButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        TextButton button = new TextButton(getImpulseScalingLabel(inputController), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                inputController.toggleImpulseScaling();
                button.setText(getImpulseScalingLabel(inputController));
            }
        });
        return button;
    }

    private static String getImpulseScalingLabel(
        SimulationInputController inputController
    ) {
        return "Impulse scaling: "
            + (inputController.getImpulseScaling()
                == SimulationInputController.ImpulseScaling.LINEAR
                    ? "Linear"
                    : "Exponential");
    }

    // Creates a toggle button that lets the next simulation click choose the camera target.
    public static TextButton createFollowObjectButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToggleButton("Follow", skin, false, inputController::setCameraFollowEnabled);
    }

    public static TextButton createResetCameraButton(
        Skin skin,
        SimulationInputController inputController
    ) {
        TextButton button = new TextButton("Reset camera", skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                inputController.resetCameraToOrigin();
            }
        });
        return button;
    }

    public static TextButton createUndoButton(
        Skin skin,
        SimulationHistory history,
        PhysicsWorld world
    ) {
        TextButton button = new TextButton("Undo", skin);
        button.setDisabled(!history.canUndo());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                history.undo(world);
            }
        });
        return button;
    }

    public static TextButton createRedoButton(
        Skin skin,
        SimulationHistory history,
        PhysicsWorld world
    ) {
        TextButton button = new TextButton("Redo", skin);
        button.setDisabled(!history.canRedo());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                history.redo(world);
            }
        });
        return button;
    }

    // Makes any supplied tools exclusive; an optional follow button clears the selected tool.
    public static void configureSimulationToolGroup(
        TextButton followObjectButton,
        TextButton... toolButtons
    ) {
        ButtonGroup<TextButton> toolGroup = new ButtonGroup<>();
        toolGroup.setMinCheckCount(0);
        toolGroup.setMaxCheckCount(1);
        for (TextButton toolButton : toolButtons) {
            toolGroup.add(toolButton);
        }

        if (followObjectButton != null) {
            followObjectButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (followObjectButton.isChecked()) {
                        toolGroup.uncheckAll();
                    }
                }
            });
        }
    }

    // Disables target buttons when the condition is active and any trigger button is checked.
    public static void updateDisabledButtons(
        TextButton[] triggerButtons,
        TextButton[] buttonsToDisable,
        boolean condition
    ) {
        boolean shouldDisable = condition;
        if (shouldDisable) {
            shouldDisable = false;
            for (TextButton triggerButton : triggerButtons) {
                if (triggerButton.isChecked()) {
                    shouldDisable = true;
                    break;
                }
            }
        }

        for (TextButton button : buttonsToDisable) {
            button.setDisabled(shouldDisable);
        }
    }

    // Updates the follow button label to show whether it is off, awaiting a target, or tracking.
    public static void updateFollowObjectButtonLabel(
        TextButton button,
        SimulationInputController inputController
    ) {
        boolean followEnabled = inputController.isCameraFollowEnabled();
        if (button.isChecked() != followEnabled) {
            button.setChecked(followEnabled);
        }
        if (!followEnabled) {
            button.setText("Follow: OFF");
        } else if (inputController.isFollowingObject()) {
            button.setText("Tracking");
        } else {
            button.setText("Select object");
        }
    }

    // Selects the given tool when checked and clears it only if it is still the selected tool.
    private static TextButton createToolButton(
        String label,
        SimulationInputController.Tool tool,
        Skin skin,
        SimulationInputController inputController
    ) {
        return createToggleButton(label, skin, false, checked -> {
            if (checked) {
                inputController.setSelectedTool(tool);
            } else if (inputController.getSelectedTool() == tool) {
                inputController.setSelectedTool(SimulationInputController.Tool.NONE);
            }
        });
    }

    // Creates a button whose label reflects whether its toggle is on or off.
    private static TextButton createToggleButton(
        String label,
        Skin skin,
        boolean initiallyChecked,
        Consumer<Boolean> onChanged
    ) {
        TextButton button = new TextButton(label + (initiallyChecked ? ": ON" : ": OFF"), skin);
        button.setChecked(initiallyChecked);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean checked = button.isChecked();
                button.setText(label + (checked ? ": ON" : ": OFF"));
                onChanged.accept(checked);
            }
        });
        return button;
    }
}
