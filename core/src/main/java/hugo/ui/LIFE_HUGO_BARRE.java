package hugo.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import hugo.entity.Life;

public final class LIFE_HUGO_BARRE {
    private LIFE_HUGO_BARRE() {
    }

    public static void draw(SpriteBatch batch, Texture pixel, Life life, float centerX, float y) {
        float healthRatio = life.getCurrentLife() / life.getMaxLife();
        float width = 36f;
        float height = 5f;
        float x = centerX - width / 2f;

        batch.setColor(Color.DARK_GRAY);
        batch.draw(pixel, x, y, width, height);

        batch.setColor(healthRatio <= 0.25f ? Color.RED : Color.GREEN);
        batch.draw(pixel, x, y, width * healthRatio, height);
        batch.setColor(Color.WHITE);
    }
}
