package hugo.bonus.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

public final class BonusAnimation implements Disposable {
    private final String assetPath;
    private final float frameDuration;
    private Texture sheet;
    private Animation<TextureRegion> animation;
    private float time = -1f;

    public BonusAnimation(String assetPath, float frameDuration) {
        this.assetPath = assetPath;
        this.frameDuration = frameDuration;
    }

    public void play() {
        if (animation == null) {
            sheet = new Texture(assetPath);
            TextureRegion[][] grid = TextureRegion.split(sheet, sheet.getWidth() / 2, sheet.getHeight() / 2);
            TextureRegion[] frames = {grid[0][0], grid[0][1], grid[1][0], grid[1][1]};
            for (TextureRegion frame : frames) {
                frame.flip(false, true);
            }
            animation = new Animation<>(frameDuration, frames);
        }
        time = 0f;
    }

    public boolean isPlaying() {
        return time >= 0f;
    }

    public void update(float delta) {
        if (isPlaying()) {
            time += delta;
            if (animation.isAnimationFinished(time)) {
                time = -1f;
            }
        }
    }

    public void draw(SpriteBatch batch, float x, float y, float width, float height, boolean facingLeft) {
        TextureRegion frame = animation.getKeyFrame(time);
        float frameWidth = width * 0.94f;
        float left = facingLeft ? x + frameWidth / 2f : x - frameWidth / 2f;
        batch.draw(frame, left, y - height * 0.98f, facingLeft ? -frameWidth : frameWidth, height * 0.94f);
    }

    @Override
    public void dispose() {
        if (sheet != null) {
            sheet.dispose();
        }
    }
}
