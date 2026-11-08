package br.edu.ufersa.thingsfall.screen.level;

import br.edu.ufersa.thingsfall.math.Vector2;
import br.edu.ufersa.thingsfall.physics.PhysicsObject;
import br.edu.ufersa.thingsfall.physics.shape.CircleShape;
import br.edu.ufersa.thingsfall.physics.shape.ConvexPolygonShape;
import br.edu.ufersa.thingsfall.screen.geometry.Geometry1;
import br.edu.ufersa.thingsfall.screen.geometry.VictoryArea;
import br.edu.ufersa.thingsfall.ui.SimulationButtons;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class Level1Screen extends LevelScreenBase {
    public Level1Screen(Game game) {
        super(game, 0);
        physicsWorld.gravity = new Vector2(0, 0);
        physicsWorld.collisionsEnabled = false;
        physicsWorld.reduceVelocityEnabled = true;

        TextButton dragObjectButton = SimulationButtons.createDragObjectButton(skin, simulationInputController);
        TextButton impulseButton = SimulationButtons.createImpulseButton(skin, simulationInputController);
        TextButton revoluteConstraintButton = SimulationButtons.createRevoluteConstraintButton(skin, simulationInputController);
        TextButton impulseScalingButton = SimulationButtons.createImpulseScalingButton(skin, simulationInputController);
        TextButton startButton = SimulationButtons.createStartRestartButton(skin, physicsWorld, () -> {
            game.setScreen(new Level1Screen(game));
            dispose();
        });

        configureTools(
            dragObjectButton,
            impulseButton,
            revoluteConstraintButton
        );

        navigationOptionsTable.row().padTop(12);
        navigationOptionsTable.add(startButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(impulseButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(impulseScalingButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(dragObjectButton).width(300).height(50);
        physicsOptionsTable.row().padTop(12);
        physicsOptionsTable.add(revoluteConstraintButton).width(300).height(50);

        PhysicsObject ball = new PhysicsObject(
            10.5, 6,
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

        physicsWorld.addGeometry(new Geometry1().spawnObjects());
        addVictoryArea(new VictoryArea(22, 0));
    }
}
