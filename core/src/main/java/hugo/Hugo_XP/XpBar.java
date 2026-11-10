package hugo.Hugo_XP;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import hugo.entity.Player;

public final class XpBar {
    public void draw(SpriteBatch batch, Texture pixel, BitmapFont font, Player player, int screenHeight) {
        float xp = player.getXp();
        float target = player.getXpToNextLevel();
        float progress = xp / target;

        float x = 20f;
        float y = screenHeight - 48f;
        float width = 220f;
        float height = 12f;

        batch.setColor(Color.DARK_GRAY);
        batch.draw(pixel, x, y, width, height);

        batch.setColor(0.40f, 0.65f, 0.25f, 1f);
        batch.draw(pixel, x, y, width * progress, height);
        batch.setColor(Color.WHITE);

        font.setColor(Color.WHITE);
        font.draw(batch, "Niveau " + player.getLevel() + "   " + Math.round(xp)
            + " / " + Math.round(target) + " XP", x, y + 30f);
    }
}
