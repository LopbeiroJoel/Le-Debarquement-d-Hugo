package hugo;

import com.badlogic.gdx.Game;

import hugo.map.MapScreen;
import hugo.startgame.StartGame;

public class HugoGame extends Game {
    public void create() {
        if (Boolean.getBoolean("hugo.smokeTest")) {
            setScreen(new MapScreen(true));
        } else {
            setScreen(new StartGame(this));
        }
    }

    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
