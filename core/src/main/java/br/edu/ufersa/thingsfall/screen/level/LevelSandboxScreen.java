package br.edu.ufersa.thingsfall.screen.level;

import br.edu.ufersa.thingsfall.ui.SimulationButtons;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class LevelSandboxScreen extends LevelScreenBase {
    public LevelSandboxScreen(Game game) {
        super(game, -1);

        TextButton staticButton = SimulationButtons.createStaticButton(skin, simulationInputController);
        TextButton dragObjectButton = SimulationButtons.createDragObjectButton(skin, simulationInputController);
        TextButton gravityButton = SimulationButtons.createGravityButton(skin, physicsWorld);
        TextButton velocityReductionButton = SimulationButtons.createVelocityReductionButton(skin, physicsWorld);
        TextButton impulseButton = SimulationButtons.createImpulseButton(skin, simulationInputController);
        TextButton revoluteConstraintButton = SimulationButtons.createRevoluteConstraintButton(skin, simulationInputController);
        TextButton impulseScalingButton = SimulationButtons.createImpulseScalingButton(skin, simulationInputController);
        TextButton collisionButton = SimulationButtons.createCollisionButton(skin, physicsWorld);

        configureTools(
            dragObjectButton,
            impulseButton,
            revoluteConstraintButton
        );
        shapeOptionsTable.row().padTop(12);
        shapeOptionsTable.add(staticButton).width(300).height(50);

        physicsOptionsTable.add(gravityButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(collisionButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(velocityReductionButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(impulseButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(impulseScalingButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(dragObjectButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(revoluteConstraintButton).width(300).height(50);
    }
}
