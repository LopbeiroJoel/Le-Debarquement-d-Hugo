package hugo.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import hugo.entity.Player;

public class UpgradeMenu {

    private static final int CHOICE_COUNT = 3;
    private static final float CARD_WIDTH = 280;
    private static final float CARD_HEIGHT = 400;
    private static final float CARD_GAP = 36; 

    private final UpgradePool pool = new UpgradePool();
    private List<Upgrade> choices = new ArrayList<>();
    private boolean open;

    private final Map<String, Texture> textures = new HashMap<>();

    private Texture texture(String path) {
        return textures.computeIfAbsent(path, p -> {
            Texture t = new Texture(p);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        });
    }

    private Texture cardTexture(Upgrade upgrade, boolean hovered) {
        String suffix = hovered ? "-hover" : "";
        return texture("upgrades/card-" + upgrade.getImageName() + suffix + ".png");
    }

    public void dispose() {
        for (Texture t : textures.values()) {
            t.dispose();
        }
        textures.clear();
    }

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
        float y = (screenHeight - CARD_HEIGHT) / 2f - 40;          
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

    public void draw(SpriteBatch batch, Texture pixel, int screenWidth, int screenHeight) {
        if (!open) {
            return;
        }

        batch.setColor(0, 0, 0, 0.62f);
        batch.draw(pixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        Rectangle first = getCardBounds(0, screenWidth, screenHeight);
        float titleY = first.y + CARD_HEIGHT + 14;
        batch.draw(texture("upgrades/title.png"), (screenWidth - 1000) / 2f, titleY, 1000, 150);

        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();

        for (int i = 0; i < choices.size(); i++) {
            Rectangle card = getCardBounds(i, screenWidth, screenHeight);
            boolean hovered = card.contains(mouseX, mouseY);
            float lift = hovered ? 20 : 0;
            batch.draw(cardTexture(choices.get(i), hovered), card.x - 10, card.y - 20 + lift, 300, 430);        }       
    }
}