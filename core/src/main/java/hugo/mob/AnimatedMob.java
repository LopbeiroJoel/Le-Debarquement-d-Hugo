package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

abstract class AnimatedMob extends Mob {
    private final Animation<TextureRegion> walk;
    private Animation<TextureRegion> action;
    private float walkTime;
    private float actionTime;
    private boolean moving;
    private boolean facingLeft;

    protected AnimatedMob(Texture texture, float x, float y, int health, int speed,
                          int size, int hitboxSize, float frameDuration) {
        super(texture, x, y, health, speed, size, size, hitboxSize, 0f);
        walk = readAnimation(texture, frameDuration);
    }

    protected static Animation<TextureRegion> readAnimation(Texture texture, float frameDuration) {
        if (texture.getWidth() % 3 != 0 || texture.getHeight() % 2 != 0) {
            throw new IllegalArgumentException("Expected a 3-column, 2-row sprite sheet");
        }

        TextureRegion[][] grid = TextureRegion.split(
            texture, texture.getWidth() / 3, texture.getHeight() / 2
        );
        TextureRegion[] frames = new TextureRegion[6];

        for (int i = 0; i < frames.length; i++) {
            frames[i] = grid[i / 3][i % 3];
            frames[i].flip(false, true);
        }

        return new Animation<>(frameDuration, frames);
    }

    protected void playAnimation(Animation<TextureRegion> animation) {
        if (!life.isDead()) {
            action = animation;
            actionTime = 0f;
        }
    }

    public void update(float deltaTime) {
        if (deltaTime <= 0f) {
            return;
        }

        if (life.isDead()) {
            action = null;
        }

        float previousX = x;
        float previousY = y;
        if (action != null) {
            setDirection(0, 0);
        }

        super.update(deltaTime);

        moving = x != previousX || y != previousY;
        walkTime = moving ? walkTime + deltaTime : 0f;
        if (x != previousX) {
            facingLeft = x < previousX;
        }

        if (action != null) {
            actionTime += deltaTime;
            if (action.isAnimationFinished(actionTime)) {
                action = null;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = action == null
            ? walk.getKeyFrame(walkTime, true)
            : action.getKeyFrame(actionTime, false);

        if (frame.isFlipX() != facingLeft) {
            frame.flip(true, false);
        }

        batch.draw(frame, x - width / 2f, y - height, width, height);
    }
}
