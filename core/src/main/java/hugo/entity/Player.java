package hugo.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import hugo.bonus.EasterEggGoku;

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
    private static final float FIRST_LEVEL_XP = 50;
    private static final float LEVEL_XP_GROWTH = 1.3f;

    private final EasterEggGoku easterEggGoku = new EasterEggGoku();
    private TextureRegion[] frames;
    private Animation<TextureRegion> walkAnimation;
    private float stateTime;

    private float xp;
    private Stat critChance;
    private Stat critMultiplier;

    private Stat fireDelay = new Stat(FIRE_DELAY);
    private float fireTimer = 0.0f;

    private int level = 1;
    private float xpToNextLevel = FIRST_LEVEL_XP;
    private int pendingUpgrades = 0;

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
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            pendingUpgrades++;
            xpToNextLevel *= LEVEL_XP_GROWTH;
        }
    }

    public int getLevel() {
        return level;
    }

    public float getXpToNextLevel() {
        return xpToNextLevel;
    }

    public int getPendingUpgrades() {
        return pendingUpgrades;
    }

    @Override
    public void update(float deltaTime) {
        float previousX = x;
        float previousY = y;
        super.update(deltaTime);

        if (dirX != 0 || dirY != 0) {
            stateTime += deltaTime;
            sprite = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            stateTime = 0;
            sprite = frames[0];
        }

        fireTimer += deltaTime;
        easterEggGoku.update(deltaTime, x != previousX || y != previousY);
    }

    public void toggleSuperSaiyan(Texture pixel) {
        easterEggGoku.toggle(this, pixel);
    }

    public void disposeSuperSaiyan() {
        easterEggGoku.dispose();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (easterEggGoku.isActive()) {
            easterEggGoku.draw(batch, x, y, facingLeft);
        } else {
            super.draw(batch);
        }
    }

    public boolean canShoot() {
        return fireTimer >= fireDelay.getValue();
    }

    public Projectile shoot(TextureRegion bulletSprite, Creature target) {
        fireTimer = 0.0f;

        float damage = attackDamage.getValue();
        if (MathUtils.random() < critChance.getValue()) {
            damage *= critMultiplier.getValue();
        }

        float dx = target.getX() - x;
        float dy = target.getCenterY() - getCenterY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            dx /= length;
            dy /= length;
        }
        return new Projectile(bulletSprite, x, getCenterY(), dx, dy, BULLET_SPEED, damage, attackRange.getValue(), BULLET_SIZE, BULLET_SIZE);
    }

    public Stat getFireDelay() {
        return fireDelay;
    }

    public void consumePendingUpgrade() {
        if (pendingUpgrades > 0) {    
            pendingUpgrades--;          
        }
    }
}
