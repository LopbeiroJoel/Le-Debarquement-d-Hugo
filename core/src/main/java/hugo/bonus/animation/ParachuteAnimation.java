package hugo.bonus.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

public final class ParachuteAnimation implements Disposable {

    private static final int CLONE_COUNT = 9;
    private static final float SPAWN_INTERVAL = 1f;
    private static final float DESCENT_TIME = 4f;
    private static final float DURATION = (CLONE_COUNT - 1) * SPAWN_INTERVAL + DESCENT_TIME;
    private static final float SPRITE_HEIGHT = 155f;
    private static final float DROP_HEIGHT = 150f;

    private TextureRegion sprite;
    private float elapsed = DURATION;

    public void play() {
        if (sprite == null) {
            Texture texture = new Texture("bonus/animation/hugo-parachute.png");
            sprite = new TextureRegion(texture);
            sprite.flip(false, true);
        }
        elapsed = 0f;
    }

    public void update(float delta) {
        elapsed = Math.min(DURATION, elapsed + delta);
    }

    public void draw(SpriteBatch batch, float hugoX, float hugoY) {
        if (elapsed >= DURATION) {
            return;
        }

        float width = SPRITE_HEIGHT * sprite.getRegionWidth() / sprite.getRegionHeight();

        for (int i = 0; i < CLONE_COUNT; i++) {
            float age = elapsed - i * SPAWN_INTERVAL;
            if (age < 0f || age >= DESCENT_TIME) {
                continue;
            }

            int slot = (i * 4) % CLONE_COUNT;
            float angle = MathUtils.PI2 * slot / CLONE_COUNT;
            float progress = age / DESCENT_TIME;
            float sway = 12f * MathUtils.sin(age * 3f) * (1f - progress);
            float x = hugoX + MathUtils.cos(angle) * 145f + sway;
            float y = hugoY + MathUtils.sin(angle) * 85f;
            float altitude = DROP_HEIGHT * (1f - progress);

            batch.draw(sprite, x - width / 2f, y - altitude - SPRITE_HEIGHT, width, SPRITE_HEIGHT);
        }
    }

    @Override
    public void dispose() {
        if (sprite != null) {
            sprite.getTexture().dispose();
        }
    }
}
