package hugo.epitech;

import com.badlogic.gdx.Game;
import hugo.epitech.screen.MapScreen;

public class HugoGame extends Game {
    public void create() {
        setScreen(new MapScreen(Boolean.getBoolean("hugo.smokeTest")));
    }
    public void dispose() {
        if(getScreen()!=null)getScreen().dispose();
    }
}
