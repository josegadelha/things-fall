package br.edu.ufersa.thingsfall.screen.level;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;
import br.edu.ufersa.thingsfall.screen.geometry.Geometry5;
import br.edu.ufersa.thingsfall.screen.geometry.VictoryArea;
import br.edu.ufersa.thingsfall.ui.SimulationButtons;
import br.edu.ufersa.thingsfall.ui.SimulationHistory;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class Level5Screen extends LevelScreenBase {
    public Level5Screen(Game game) {
        this(game, new SimulationHistory());
    }

    public Level5Screen(Game game, SimulationHistory simulationHistory) {
        super(game, 4, simulationHistory);
        physicsWorld.gravity = new Vector2(0, 0);
        physicsWorld.collisionsEnabled = false;
        physicsWorld.reduceVelocityEnabled = true;

        TextButton dragObjectButton = SimulationButtons.createDragObjectButton(skin, simulationInputController);
        TextButton impulseButton = SimulationButtons.createImpulseButton(skin, simulationInputController);
        TextButton revoluteConstraintButton = SimulationButtons.createRevoluteConstraintButton(skin, simulationInputController);
        TextButton impulseScalingButton = SimulationButtons.createImpulseScalingButton(skin, simulationInputController);
        TextButton startButton = SimulationButtons.createStartRestartButton(
            skin,
            physicsWorld,
            this::onSimulationStarted,
            () -> restartLevel(new Level5Screen(game, simulationInputController.getHistory()))
        );
        configureNavigation(startButton);

        configureTools(
            dragObjectButton,
            impulseButton,
            revoluteConstraintButton
        );

        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(impulseButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(impulseScalingButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(dragObjectButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(revoluteConstraintButton).width(300).height(50);

        PhysicsObject ball = new PhysicsObject(
            -15, 5,
            false,
            1.0,
            1.0,
            Color.valueOf("a8d8b0"),
            true,
            // body
            new CircleShape(0.5, new Vector2(0, 0)),

            // 6 triangles: base on the circumference, tip toward the center
            new ConvexPolygonShape(new Vector2(0.4830, -0.1294), new Vector2(0.4830, 0.1294), new Vector2(0.15, 0.0)),
            new ConvexPolygonShape(new Vector2(0.3536, 0.3536), new Vector2(0.1294, 0.4830), new Vector2(0.075, 0.1299)),
            new ConvexPolygonShape(new Vector2(-0.1294, 0.4830), new Vector2(-0.3536, 0.3536), new Vector2(-0.075, 0.1299)),
            new ConvexPolygonShape(new Vector2(-0.4830, 0.1294), new Vector2(-0.4830, -0.1294), new Vector2(-0.15, 0.0)),
            new ConvexPolygonShape(new Vector2(-0.3536, -0.3536), new Vector2(-0.1294, -0.4830), new Vector2(-0.075, -0.1299)),
            new ConvexPolygonShape(new Vector2(0.1294, -0.4830), new Vector2(0.3536, -0.3536), new Vector2(0.075, -0.1299))
        );
        ball.renderDetails = false;
        physicsWorld.objects.add(ball);
        simulationInputController.setDirectManipulationExcludedObject(ball);

        physicsWorld.addGeometry(new Geometry5().spawnObjects());
        addVictoryArea(new VictoryArea(8, 0.2));
        simulationHistory.restore(physicsWorld);
    }
}
