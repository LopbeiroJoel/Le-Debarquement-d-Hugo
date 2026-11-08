package hugo.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player extends Creature {

    private static final float MAX_LIFE = 100;
    private static final float BASE_SPEED = 400;
    private static final float WIDTH = 72;
    private static final float HEIGHT = 96;
    private static final int FRAME_WIDTH = 543;
    private static final int FRAME_HEIGHT = 724;
    private static final float ATTACK_RANGE = 500;
    private static final float ATTACK_DAMAGE = 7;
    private static final float HITBOX_SIZE = 33;
    private static final float CRIT_CHANCE = 0.1f;
    private static final float CRIT_MULTIPLIER = 2.0f;
    private static final float FRAME_DURATION = 0.15f;
    private static final float FIRE_DELAY = 1.0f;
    private static final float BULLET_SPEED = 1000.0f;
    private static final float BULLET_SIZE = 8.0f;

    private TextureRegion[] frames;
    private Animation<TextureRegion> walkAnimation;
    private float stateTime;

    private float xp;
    private Stat critChance;
    private Stat critMultiplier;

    private Stat fireDelay = new Stat(FIRE_DELAY);
    private float fireTimer = 0.0f;

    public Player(Texture sheet, float x, float y) {
        super(new TextureRegion(sheet, 0, 0, FRAME_WIDTH, FRAME_HEIGHT), x, y, MAX_LIFE, BASE_SPEED, WIDTH, HEIGHT, ATTACK_RANGE, ATTACK_DAMAGE, HITBOX_SIZE);
        this.xp = 0;
        this.critChance = new Stat(CRIT_CHANCE);
        this.critMultiplier = new Stat(CRIT_MULTIPLIER);
        frames = TextureRegion.split(sheet, FRAME_WIDTH, FRAME_HEIGHT)[0];
        for (TextureRegion frame : frames) {
            frame.flip(false, true);
        }
        walkAnimation = new Animation<>(FRAME_DURATION, frames);
        stateTime = 0;

    }

    public float getXp() {
        return xp;
    }

    public Stat getCritChance() {
        return critChance;
    }

    public Stat getCritMultiplier() {
        return critMultiplier;
    }

    public void addXp(float amount) {
        if (amount < 0) {
            return;
        }
        this.xp += amount;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (dirX != 0 || dirY != 0) {
            stateTime += deltaTime;
            sprite = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            stateTime = 0;
            sprite = frames[0];
        }

        fireTimer += deltaTime;
    }

    public boolean canShoot() {
        return fireTimer >= fireDelay.getValue();
    }

    public Projectile shoot(TextureRegion bulletSprite) {
        fireTimer = 0.0f;

        float shootDirX = facingLeft ? -1 : 1;
        return new Projectile(bulletSprite, x, getCenterY(), shootDirX, 0, BULLET_SPEED, attackDamage.getValue(), attackRange.getValue(), BULLET_SIZE, BULLET_SIZE);
    }

    public Stat getFireDelay() {
        return fireDelay;
    }
}
