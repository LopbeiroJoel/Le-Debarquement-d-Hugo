package hugo.chest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import hugo.bonus.FullHealBonus;
import hugo.entity.Player;

public final class ChestBonusMenu {
    private final FullHealBonus fullHealBonus = new FullHealBonus();
    private final GlyphLayout label = new GlyphLayout();
    private final Rectangle closeButton = new Rectangle();
    private Texture heartIcon;
    private Texture mateIcon;
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

    public void update(Player player, int width, int height) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            close();
            return;
        }

        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = height - Gdx.input.getY();
            placeCloseButton(width, height);

            if (getCardBounds(0, width, height).contains(mouseX, mouseY)) {
                player.playFullHealAnimation();
                fullHealBonus.apply(player);
                close();
            } else if (getCardBounds(1, width, height).contains(mouseX, mouseY)) {
                player.drinkMate();
                close();
            } else if (closeButton.contains(mouseX, mouseY)) {
                close();
            }
        }
    }

    public void draw(SpriteBatch batch, Texture pixel, BitmapFont font, int width, int height) {
        if (!open) {
            return;
        }

        if (heartIcon == null) {
            heartIcon = new Texture("bonus/heart.png");
        }
        if (mateIcon == null) {
            mateIcon = new Texture("bonus/mate.png");
        }
        batch.setColor(0, 0, 0, 0.62f);
        batch.draw(pixel, 0, 0, width, height);
        font.setColor(Color.WHITE);
        drawCentered(batch, font, "Coffre HUGO", width / 2f, height * 0.72f);

        for (int i = -1; i <= 1; i++) {
            Rectangle card = getCardBounds(i, width, height);
            batch.setColor(0.28f, 0.26f, 0.16f, 1);
            batch.draw(pixel, card.x, card.y, card.width, card.height);
            float centerX = card.x + card.width / 2f;
            if (i >= 0) {
                Texture icon = i == 0 ? heartIcon : mateIcon;
                String name = i == 0 ? "Soin complet" : "Maté";
                float iconSize = Math.min(card.width * 0.70f, card.height * 0.60f);
                batch.setColor(Color.WHITE);
                batch.draw(icon, centerX - iconSize / 2f, card.y + card.height * 0.30f, iconSize, iconSize);
                drawCentered(batch, font, name, centerX, card.y + card.height * 0.24f);
            } else {
                drawCentered(batch, font, "Bonus a venir", centerX, height / 2f + 5);
            }
        }

        placeCloseButton(width, height);
        batch.setColor(0.36f, 0.39f, 0.22f, 1);
        batch.draw(pixel, closeButton.x, closeButton.y, closeButton.width, closeButton.height);
        batch.setColor(Color.WHITE);
        drawCentered(batch, font, "Continuer", width / 2f, closeButton.y + 27);
    }

    public void dispose() {
        if (heartIcon != null) {
            heartIcon.dispose();
        }
        if (mateIcon != null) {
            mateIcon.dispose();
        }
    }

    private Rectangle getCardBounds(int index, int width, int height) {
        float cardWidth = Math.min(170f, width / 4f);
        float cardHeight = height * 0.27f;
        float gap = cardWidth * 0.12f;
        float x = (width - cardWidth) / 2f + index * (cardWidth + gap);
        float y = (height - cardHeight) / 2f;
        return new Rectangle(x, y, cardWidth, cardHeight);
    }

    private void placeCloseButton(int width, int height) {
        closeButton.set(width / 2f - 65, height * 0.20f, 130, 42);
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float centerX, float y) {
        label.setText(font, text);
        font.draw(batch, label, centerX - label.width / 2f, y);
    }
}
