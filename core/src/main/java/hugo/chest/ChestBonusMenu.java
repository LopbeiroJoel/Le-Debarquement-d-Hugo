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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public final class ChestBonusMenu {
    private static final int CARD_COUNT = 3;

    private final FullHealBonus fullHealBonus = new FullHealBonus();
    private final GlyphLayout label = new GlyphLayout();
    private final Rectangle closeButton = new Rectangle();
    private final EnumMap<Bonus, Texture> icons = new EnumMap<>(Bonus.class);
    private boolean open;

    // bonus cards
    private enum Bonus {
        TOBACCO("Tobacco", "Cigarette", "tobacco.png"),
        FULL_HEAL("Full heal", "", "heart.png"),
        MATE("Mate", "", "mate.png"),
        HELMET("Helmet", "Invincible for 30 s", "helmet.png"),
        PARACHUTE("Parachute", "Defeat all mobs", "parachute.png"),
        WHISKY("Whisky", "Drunk effect for 30 s", "whisky.png");

        final String title;
        final String description;
        final String iconFile;

        Bonus(String title, String description, String iconFile) {
            this.title = title;
            this.description = description;
            this.iconFile = iconFile;
        }
    }

    private List<Bonus> choices = List.of();

    public boolean isOpen() {
        return open;
    }

    // pick three bonuses
    public void open() {
        if (open) {
            return;
        }

        List<Bonus> available = new ArrayList<>(List.of(Bonus.values()));
        Collections.shuffle(available);
        choices = List.copyOf(available.subList(0, CARD_COUNT));
        open = true;
    }

    public void close() {
        open = false;
    }

    // click a card
    public void update(Player player, int width, int height) {
        if (!open) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            close();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        float mouseX = Gdx.input.getX();
        float mouseY = height - Gdx.input.getY();
        placeCloseButton(width, height);

        for (int i = 0; i < choices.size(); i++) {
            if (getCardBounds(i, width, height).contains(mouseX, mouseY)) {
                applyBonus(choices.get(i), player);
                close();
                return;
            }
        }

        if (closeButton.contains(mouseX, mouseY)) {
            close();
        }
    }

    // apply bonus
    private void applyBonus(Bonus bonus, Player player) {
        switch (bonus) {
            case TOBACCO -> player.useTobacco();
            case FULL_HEAL -> {
                player.playFullHealAnimation();
                fullHealBonus.apply(player);
            }
            case MATE -> player.drinkMate();
            case HELMET -> player.equipHelmet();
            case PARACHUTE -> player.useParachute();
            case WHISKY -> player.drinkWhisky();
        }
    }

    // chest menu
    public void draw(SpriteBatch batch, Texture pixel, BitmapFont font, int width, int height) {
        if (!open) {
            return;
        }

        batch.setColor(0, 0, 0, 0.62f);
        batch.draw(pixel, 0, 0, width, height);
        font.setColor(Color.WHITE);
        drawCentered(batch, font, "HUGO chest", width / 2f, height * 0.72f);

        for (int i = 0; i < choices.size(); i++) {
            Rectangle card = getCardBounds(i, width, height);
            batch.setColor(0.28f, 0.26f, 0.16f, 1);
            batch.draw(pixel, card.x, card.y, card.width, card.height);
            float centerX = card.x + card.width / 2f;

            Bonus bonus = choices.get(i);

            Texture icon = icons.get(bonus);
            if (icon == null) {
                icon = new Texture("bonus/" + bonus.iconFile);
                icons.put(bonus, icon);
            }

            float iconSize = Math.min(card.width * 0.70f, card.height * 0.54f);
            batch.setColor(Color.WHITE);
            batch.draw(
                    icon,
                    centerX - iconSize / 2f,
                    card.y + card.height * 0.40f,
                    iconSize,
                    iconSize);
            drawCentered(batch, font, bonus.title, centerX, card.y + card.height * 0.33f);
            if (!bonus.description.isEmpty()) {
                drawCentered(batch, font, bonus.description, centerX, card.y + card.height * 0.17f);
            }
        }

        placeCloseButton(width, height);
        batch.setColor(0.36f, 0.39f, 0.22f, 1);
        batch.draw(pixel, closeButton.x, closeButton.y, closeButton.width, closeButton.height);
        batch.setColor(Color.WHITE);
        drawCentered(batch, font, "Continue", width / 2f, closeButton.y + 27);
    }

    public void dispose() {
        for (Texture icon : icons.values()) {
            icon.dispose();
        }
        icons.clear();
    }

    private Rectangle getCardBounds(int index, int width, int height) {
        float cardWidth = Math.min(170f, width / 4f);
        float cardHeight = height * 0.27f;
        float gap = cardWidth * 0.12f;
        float x = (width - cardWidth) / 2f + (index - 1) * (cardWidth + gap);
        float y = (height - cardHeight) / 2f;
        return new Rectangle(x, y, cardWidth, cardHeight);
    }

    private void placeCloseButton(int width, int height) {
        closeButton.set(width / 2f - 65, height * 0.20f, 130, 42);
    }

    private void drawCentered(
            SpriteBatch batch, BitmapFont font, String text, float centerX, float y) {
        label.setText(font, text);
        font.draw(batch, label, centerX - label.width / 2f, y);
    }
}
