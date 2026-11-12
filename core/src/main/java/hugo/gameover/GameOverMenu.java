package hugo.gameover;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class GameOverMenu {

    private static final float PANEL_WIDTH = 520;
    private static final float PANEL_HEIGHT = 240;
    private static final float BUTTON_WIDTH = 280;
    private static final float BUTTON_HEIGHT = 80;

    private boolean open;
    private int level;
    private int wave;
    private int kills;

    private final Map<String, Texture> textures = new HashMap<>();

    private Texture texture(String path) {
        return textures.computeIfAbsent(path, p -> {
            Texture t = new Texture(p);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        });
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

    private void drawNumber(SpriteBatch batch, int value, float rightX, float centerY) {
        String text = String.valueOf(value);
        float x = rightX - 30 * text.length();
        for (char c : text.toCharArray()) {
            batch.draw(texture("gameover/digits/" + c + ".png"), x, centerY - 22, 30, 44);
            x += 30;
        }
    }

    public void draw(SpriteBatch batch, Texture pixel, int screenWidth, int screenHeight) {
        if (!open) {
            return;
        }

        // 1. voile sombre, un peu plus opaque que pour le menu
        batch.setColor(0, 0, 0, 0.72f);
        batch.draw(pixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        Rectangle panel = getPanelBounds(screenWidth, screenHeight);
        Rectangle button = getButtonBounds(screenWidth, screenHeight);

        // 2. titre et panneau
        batch.draw(texture("gameover/title.png"), (screenWidth - 1000) / 2f, panel.y + PANEL_HEIGHT + 20, 1000, 150);
        batch.draw(texture("gameover/panel.png"), panel.x - 10, panel.y - 20, 540, 270);

        // 3. les trois nombres
        float top = panel.y + PANEL_HEIGHT;
        float right = panel.x + 480;
        drawNumber(batch, level, right, top - 64);
        drawNumber(batch, wave, right, top - 120);
        drawNumber(batch, kills, right, top - 176);

        // 4. le bouton, normal ou survolé
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        String image = hovered ? "gameover/button-replay-hover.png" : "gameover/button-replay.png";
        batch.draw(texture(image), button.x - 10, button.y - 20, 300, 110);
    }

    public void open(int level, int wave, int kills) {
        this.level = level;
        this.wave = wave;
        this.kills = kills;
        open = true;
    }

    public Rectangle getPanelBounds(int screenWidth, int screenHeight) {
        float x = (screenWidth - PANEL_WIDTH) / 2f;
        float y = (screenHeight - PANEL_HEIGHT) / 2f;
        return new Rectangle(x, y, PANEL_WIDTH, PANEL_HEIGHT);
    }

    public Rectangle getButtonBounds(int screenWidth, int screenHeight) {
        Rectangle panel = getPanelBounds(screenWidth, screenHeight);
        float x = (screenWidth - BUTTON_WIDTH) / 2f;
        float y = panel.y - 40 - BUTTON_HEIGHT;
        return new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    // Renvoie true si le joueur vient de cliquer sur REJOUER
    public boolean update(int screenWidth, int screenHeight) {
        if (!open || !Gdx.input.justTouched()) {
            return false;
        }
        float mouseX = Gdx.input.getX();
        float mouseY = screenHeight - Gdx.input.getY();
        return getButtonBounds(screenWidth, screenHeight).contains(mouseX, mouseY);
    }
}
