package hugo.upgrade;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

import hugo.entity.Player;

public class UpgradeMenu {

    private static final int CHOICE_COUNT = 3;
    private static final float CARD_WIDTH = 260;
    private static final float CARD_HEIGHT = 140;
    private static final float CARD_GAP = 30; 

    private final UpgradePool pool = new UpgradePool();
    private List<Upgrade> choices = new ArrayList<>();
    private boolean open;

    public boolean isOpen() {
        return open;
    }

    public void open() {
        choices = pool.pickRandom(CHOICE_COUNT);    
        open = true;
    }

    public Rectangle getCardBounds(int index, int screenWidth, int screenHeight) {
        float totalWidth = CHOICE_COUNT * CARD_WIDTH + (CHOICE_COUNT - 1) * CARD_GAP;
        float startX = (screenWidth - totalWidth) / 2f;      
        float x = startX + index * (CARD_WIDTH + CARD_GAP);        
        float y = (screenHeight - CARD_HEIGHT) / 2f;          
        return new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT);
    }

    public void update(Player player, int screenWidth, int screenHeight) {
        if (!open) {
            return;
        }

        int chosen = -1;                             
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();        
            float mouseY = screenHeight - Gdx.input.getY();  
            for (int i = 0; i < choices.size(); i++) {
                if (getCardBounds(i, screenWidth, screenHeight).contains(mouseX, mouseY)) {
                    chosen = i;                        
                }
            }
        }

        if (chosen < 0) {
            return;                                  
        }

        choices.get(chosen).apply(player);                   
        player.consumePendingUpgrade();                               

        if (player.getPendingUpgrades() > 0) {
            open();                                        
        } else {
            open = false;                                   
        }
    }

    public List<Upgrade> getChoices() {                  
        return choices;
    }
}