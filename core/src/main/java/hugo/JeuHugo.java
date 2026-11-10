package hugo;

import com.badlogic.gdx.Game;
import hugo.map.EcranCarte;
import hugo.startgame.StartGame;

public class JeuHugo extends Game {
    public void create() {
        if (Boolean.getBoolean("hugo.smokeTest")) {
            setScreen(new EcranCarte(true));
        } else {
            setScreen(new StartGame(this));
        }
    }
    public void dispose() {
        if(getScreen()!=null)getScreen().dispose();
    }
}
