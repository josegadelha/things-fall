package br.edu.ufersa.thingsfall;

import br.edu.ufersa.thingsfall.screen.TitleScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;


public class ThingsFall extends Game {
    @Override
    public void create() {
        setScreen(new TitleScreen(this));
    }

    @Override
    public void dispose() {
        Screen currentScreen = getScreen();
        super.dispose();
        if (currentScreen != null) currentScreen.dispose();
    }
}
