package hugo;

import com.badlogic.gdx.Game;
import hugo.map.EcranCarte;

public class JeuHugo extends Game {
    public void create() {
        setScreen(new EcranCarte(Boolean.getBoolean("hugo.smokeTest")));
    }
    public void dispose() {
        if(getScreen()!=null)getScreen().dispose();
    }
}
