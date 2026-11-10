package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MobLvl2 extends Mob {
    private static final int SPRITE_SIZE = 76;

    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> biteAnimation;
    private final TextureRegion idleFrame;

    private float walkTime;
    private float biteTime = -1f;
    private boolean moving;
    private boolean facingLeft;

    public MobLvl2(Texture walkTexture, Texture biteTexture, float x, float y) {
        super(walkTexture, x, y, 7, 320, SPRITE_SIZE, SPRITE_SIZE, 18, 0f);
        walkAnimation = new Animation<>(0.10f, readFrames(walkTexture));
        TextureRegion[] biteFrames = readFrames(biteTexture);
        biteAnimation = new Animation<>(0.09f, biteFrames);
        idleFrame = biteFrames[0];
    }

    private static TextureRegion[] readFrames(Texture texture) {
        if (texture.getWidth() % 3 != 0 || texture.getHeight() % 2 != 0) {
            throw new IllegalArgumentException("Expected a 3-column, 2-row sprite sheet");
        }

        TextureRegion[][] grid = TextureRegion.split(
            texture, texture.getWidth() / 3, texture.getHeight() / 2
        );
        TextureRegion[] frames = new TextureRegion[6];

        for (int index = 0; index < frames.length; index++) {
            frames[index] = grid[index / 3][index % 3];
            frames[index].flip(false, true);
        }

        return frames;
    }

    public void update(float deltaTime) {
        if (deltaTime <= 0) {
            return;
        }

        float previousX = x;
        float previousY = y;

        if (isBiting()) {
            setDirection(0, 0);
        }

        super.update(deltaTime);
        moving = x != previousX || y != previousY;

        if (x != previousX) {
            facingLeft = x < previousX;
        }

        if (life.isDead()) {
            moving = false;
            biteTime = -1f;
        } else if (isBiting()) {
            biteTime += deltaTime;
            if (biteAnimation.isAnimationFinished(biteTime)) {
                biteTime = -1f;
            }
        }

        walkTime = moving ? walkTime + deltaTime : 0f;
    }

    protected void onAttack() {
        biteTime = 0f;
        moving = false;
        setDirection(0, 0);
    }

    public boolean isBiting() {
        return biteTime >= 0;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = idleFrame;

        if (isBiting()) {
            frame = biteAnimation.getKeyFrame(biteTime, false);
        } else if (moving) {
            frame = walkAnimation.getKeyFrame(walkTime, true);
        }

        if (frame.isFlipX() != facingLeft) {
            frame.flip(true, false);
        }

        batch.draw(frame, x - width / 2f, y - height, width, height);
    }
}
