package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MobLvl1 extends Mob {

    public static final int SPRITE_WIDTH = 65;
    public static final int SPRITE_HEIGHT = 86;

    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> attackAnimation;
    private final TextureRegion idleFrame;

    private float walkTime = 0;
    private float attackTime = -1;
    private boolean moving = false;
    private boolean facingLeft = false;

    public MobLvl1(Texture texture, float x, float y) {
        super(texture, x, y, 10, 100, SPRITE_WIDTH, SPRITE_HEIGHT, 5);

        if (texture.getWidth() % 4 != 0 || texture.getHeight() % 2 != 0) {
            throw new IllegalArgumentException("Expected a 4-column, 2-row sprite sheet");
        }

        int frameWidth = texture.getWidth() / 4;
        int frameHeight = texture.getHeight() / 2;
        TextureRegion[][] frames = TextureRegion.split(texture, frameWidth, frameHeight);

        for (TextureRegion[] row : frames) {
            for (TextureRegion frame : row) {
                frame.flip(false, true);
            }
        }

        walkAnimation = new Animation<>(0.14f, frames[0]);
        attackAnimation = new Animation<>(0.12f, frames[1]);
        idleFrame = frames[1][0];
    }

    public void update(float deltaTime) {
        if (deltaTime <= 0) {
            return;
        }

        float previousX = x;
        float previousY = y;

        if (isAttacking()) {
            setDirection(0, 0);
        }

        super.update(deltaTime);

        moving = x != previousX || y != previousY;

        if (x != previousX) {
            facingLeft = x < previousX;
        }

        if (life.isDead()) {
            moving = false;
            attackTime = -1;
        } else if (isAttacking()) {
            attackTime = attackTime + deltaTime;

            if (attackAnimation.isAnimationFinished(attackTime)) {
                attackTime = -1;
            }
        }

        if (moving) {
            walkTime = walkTime + deltaTime;
        } else {
            walkTime = 0;
        }
    }

    protected void onAttack() {
        playBatonAttack();
    }

    public void playBatonAttack() {
        if (life.isDead()) {
            return;
        }

        attackTime = 0;
        moving = false;
        setDirection(0, 0);
    }

    public boolean isAttacking() {
        return attackTime >= 0;
    }

    public boolean isMoving() {
        return moving;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = idleFrame;

        if (isAttacking()) {
            frame = attackAnimation.getKeyFrame(attackTime, false);
        } else if (moving) {
            frame = walkAnimation.getKeyFrame(walkTime, true);
        }

        if (frame.isFlipX() != facingLeft) {
            frame.flip(true, false);
        }
        batch.draw(frame, x - width / 2f, y - height, width, height);
    }
}
