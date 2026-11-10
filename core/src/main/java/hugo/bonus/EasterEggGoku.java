package hugo.bonus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import hugo.entity.Player;

public final class EasterEggGoku {
    private static final float SPEED_MULTIPLIER = 2.5f;
    private static final float DAMAGE_MULTIPLIER = 12f;
    private static final float FIRE_DELAY_MULTIPLIER = 0.08f;
    private static final float SPRITE_WIDTH = 146f;
    private static final float SPRITE_HEIGHT = 164f;
    private static final float FEET_OFFSET = 138f;

    private TextureRegion sprite;
    private Texture walkSheet;
    private Animation<TextureRegion> walk;
    private Texture pixel;
    private boolean active;
    private float auraTime;
    private float walkTime;
    private boolean moving;

    public void toggle(Player player, Texture pixel) {
        if (active) {
            player.getSpeed().removeMultiplier(SPEED_MULTIPLIER);
            player.getAttackDamage().removeMultiplier(DAMAGE_MULTIPLIER);
            player.getFireDelay().removeMultiplier(FIRE_DELAY_MULTIPLIER);
        } else {
            if (sprite == null) {
                sprite = new TextureRegion(new Texture("easteregg/hugo-super-saiyan.png"));
                sprite.flip(false, true);
                walkSheet = new Texture("easteregg/hugo-super-saiyan-walk.png");
                TextureRegion[][] grid = TextureRegion.split(walkSheet, walkSheet.getWidth() / 2, walkSheet.getHeight() / 2);
                TextureRegion[] frames = {grid[0][0], grid[0][1], grid[1][0], grid[1][1]};
                for (TextureRegion frame : frames) {
                    frame.flip(false, true);
                }
                walk = new Animation<>(0.09f, frames);
            }
            this.pixel = pixel;
            auraTime = 0f;
            walkTime = 0f;
            moving = false;
            player.getSpeed().addMultiplier(SPEED_MULTIPLIER);
            player.getAttackDamage().addMultiplier(DAMAGE_MULTIPLIER);
            player.getFireDelay().addMultiplier(FIRE_DELAY_MULTIPLIER);
        }
        active = !active;
    }

    public boolean isActive() {
        return active;
    }

    public void update(float delta, boolean moving) {
        if (active) {
            auraTime = (auraTime + delta) % 8f;
            this.moving = moving;
            walkTime = moving ? (walkTime + delta) % walk.getAnimationDuration() : 0f;
        }
    }

    public void draw(SpriteBatch batch, float x, float y, boolean facingLeft) {
        float spriteX = facingLeft ? x + SPRITE_WIDTH / 2f : x - SPRITE_WIDTH / 2f;
        float width = facingLeft ? -SPRITE_WIDTH : SPRITE_WIDTH;
        TextureRegion frame = moving ? walk.getKeyFrame(walkTime, true) : sprite;
        float feetOffset = moving ? 142f : FEET_OFFSET;
        batch.draw(frame, spriteX, y - feetOffset, width, SPRITE_HEIGHT);

        for (int i = 0; i < 14; i++) {
            float phase = (auraTime * 1.5f + i / 14f) % 1f;
            float side = i % 2 == 0 ? -1f : 1f;
            float wave = MathUtils.sin((phase + i * 0.13f) * MathUtils.PI2);
            float sparkX = x + side * (45f + 13f * wave);
            float sparkY = y + 5f - phase * 135f;
            batch.setColor(1f, 0.78f, 0.22f, 1f - phase);
            batch.draw(pixel, sparkX, sparkY, 2f, 4f + 4f * phase);
        }
        float waveProgress = auraTime % 0.8f / 0.8f;
        float radius = 24f + 60f * waveProgress;
        batch.setColor(1f, 0.86f, 0.40f, 0.8f * (1f - waveProgress));
        for (int i = 0; i < 36; i++) {
            float angle = i * MathUtils.PI2 / 36f;
            float waveX = x + MathUtils.cos(angle) * radius;
            float waveY = y + MathUtils.sin(angle) * radius * 0.24f;
            batch.draw(pixel, waveX, waveY, 3f, 2f);
        }
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        if (sprite != null) {
            sprite.getTexture().dispose();
            walkSheet.dispose();
        }
    }
}
