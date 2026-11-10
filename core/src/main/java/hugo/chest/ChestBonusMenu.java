package hugo.chest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public final class ChestBonusMenu {
    private final GlyphLayout label = new GlyphLayout();
    private final Rectangle closeButton = new Rectangle();
    private boolean open;

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public void update(int width, int height) {
        placeCloseButton(width, height);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || (Gdx.input.justTouched()
                    && closeButton.contains(Gdx.input.getX(), height - Gdx.input.getY()))) {
            close();
        }
    }

    public void draw(SpriteBatch batch, Texture pixel, BitmapFont font, int width, int height) {
        if (!open) {
            return;
        }

        batch.setColor(0, 0, 0, 0.62f);
        batch.draw(pixel, 0, 0, width, height);
        font.setColor(Color.WHITE);
        drawCentered(batch, font, "Coffre HUGO", width / 2f, height * 0.72f);

        float cardWidth = Math.min(170f, width / 4f);
        float cardHeight = height * 0.27f;
        float gap = cardWidth * 0.12f;
        for (int i = -1; i <= 1; i++) {
            float centerX = width / 2f + i * (cardWidth + gap);
            float cardY = (height - cardHeight) / 2f;
            batch.setColor(0.28f, 0.26f, 0.16f, 1);
            batch.draw(pixel, centerX - cardWidth / 2f, cardY, cardWidth, cardHeight);
            drawCentered(batch, font, "Bonus a venir", centerX, height / 2f + 5);
        }

        placeCloseButton(width, height);
        batch.setColor(0.36f, 0.39f, 0.22f, 1);
        batch.draw(pixel, closeButton.x, closeButton.y, closeButton.width, closeButton.height);
        batch.setColor(Color.WHITE);
        drawCentered(batch, font, "Continuer", width / 2f, closeButton.y + 27);
    }

    private void placeCloseButton(int width, int height) {
        closeButton.set(width / 2f - 65, height * 0.20f, 130, 42);
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float centerX, float y) {
        label.setText(font, text);
        font.draw(batch, label, centerX - label.width / 2f, y);
    }
}
