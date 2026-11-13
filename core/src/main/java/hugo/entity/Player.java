package hugo.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import hugo.bonus.EasterEggGoku;
import hugo.bonus.HelmetBonus;
import hugo.bonus.MateBonus;
import hugo.bonus.TobaccoBonus;
import hugo.bonus.WhiskyBonus;
import hugo.bonus.animation.BonusAnimation;
import hugo.bonus.animation.ParachuteAnimation;
import hugo.ui.LIFE_HUGO_BARRE;

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
    private static final float BULLET_SPRITE_WIDTH = 24f;
    private static final float BULLET_SPRITE_HEIGHT = 9f;
    private static final float FIRST_LEVEL_XP = 50;
    private static final float LEVEL_XP_GROWTH = 1.3f;

    private final HelmetBonus helmetBonus = new HelmetBonus();
    private final MateBonus mateBonus = new MateBonus();
    private final TobaccoBonus tobaccoBonus = new TobaccoBonus();
    private final WhiskyBonus whiskyBonus = new WhiskyBonus();
    private final Matrix4 normalTransform = new Matrix4();
    private final Matrix4 whiskyTransform = new Matrix4();
    private final BonusAnimation fullHealAnimation = new BonusAnimation("bonus/animation/hugo-full-heal.png", 0.16f);
    private final BonusAnimation mateAnimation = new BonusAnimation("bonus/animation/hugo-drinks-mate.png", 0.5f);
    private final BonusAnimation tobaccoAnimation = new BonusAnimation("bonus/animation/hugo-smokes.png", 0.45f);
    private final BonusAnimation helmetAnimation = new BonusAnimation("bonus/animation/hugo-equips-helmet.png", 0.20f);
    private final ParachuteAnimation parachuteAnimation = new ParachuteAnimation();
    private final BonusAnimation whiskyAnimation = new BonusAnimation("bonus/animation/hugo-drinks-whisky.png", 0.36f);
    private boolean parachuteRequested;
    private boolean parachuteStrike;
    private TextureRegion wornHelmet;
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
    private int kills = 0;


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
        parachuteStrike = parachuteRequested;
        parachuteRequested = false;
        parachuteAnimation.update(deltaTime);

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
        if (helmetAnimation.isPlaying()) {
            helmetAnimation.update(deltaTime);
            if (!helmetAnimation.isPlaying()) {
                helmetBonus.activate();
            }
        } else {
            helmetBonus.update(deltaTime);
        }
        if (tobaccoAnimation.isPlaying()) {
            tobaccoAnimation.update(deltaTime);
            if (!tobaccoAnimation.isPlaying()) {
                tobaccoBonus.activate(this);
            }
        } else {
            tobaccoBonus.update(this, deltaTime);
        }
        fullHealAnimation.update(deltaTime);
        whiskyBonus.update(deltaTime);
        if (whiskyAnimation.isPlaying()) {
            whiskyAnimation.update(deltaTime);
            if (!whiskyAnimation.isPlaying()) {
                whiskyBonus.activate();
            }
        }
        if (mateAnimation.isPlaying()) {
            mateAnimation.update(deltaTime);
            if (!mateAnimation.isPlaying()) {
                mateBonus.activate(this);
            }
        } else {
            mateBonus.update(this, deltaTime);
        }
        easterEggGoku.update(deltaTime, x != previousX || y != previousY);
    }

    public void toggleSuperSaiyan(Texture pixel) {
        easterEggGoku.toggle(this, pixel);
    }

    public void playFullHealAnimation() {
        fullHealAnimation.play();
    }

    public void drinkMate() {
        mateAnimation.play();
    }

    public void useTobacco() {
        tobaccoAnimation.play();
    }

    public void drinkWhisky() {
        whiskyAnimation.play();
    }

    public float getWhiskyTime() {
        return whiskyBonus.getRemainingTime();
    }

    public void useParachute() {
        parachuteAnimation.play();
        parachuteRequested = true;
    }

    public boolean hasParachuteStrike() {
        return parachuteStrike;
    }

    public void equipHelmet() {
        if (helmetBonus.isActive()) {
            helmetBonus.activate();
            return;
        }
        if (wornHelmet == null) {
            wornHelmet = new TextureRegion(new Texture("bonus/animation/helmet-worn.png"));
            wornHelmet.flip(false, true);
        }
        helmetAnimation.play();
    }

    @Override
    public void takeDamage(float damage) {
        if (!helmetBonus.isActive()) {
            super.takeDamage(damage);
        }
    }

    public void disposeAnimations() {
        parachuteAnimation.dispose();
        whiskyAnimation.dispose();
        helmetAnimation.dispose();
        if (wornHelmet != null) {
            wornHelmet.getTexture().dispose();
        }
        tobaccoAnimation.dispose();
        mateAnimation.dispose();
        fullHealAnimation.dispose();
        easterEggGoku.dispose();
    }

    @Override
    public void draw(SpriteBatch batch) {
        parachuteAnimation.draw(batch, x, y);

        float whiskyTime = getWhiskyTime();
        if (whiskyTime > 0f) {
            float lean = 9f * MathUtils.sin(whiskyTime * 5f) * Math.min(1f, whiskyTime);
            normalTransform.set(batch.getTransformMatrix());
            whiskyTransform.set(normalTransform).translate(x, y, 0f)
                .rotate(Vector3.Z, lean).translate(-x, -y, 0f);
            batch.setTransformMatrix(whiskyTransform);
            drawBody(batch);
            batch.setTransformMatrix(normalTransform);
        } else {
            drawBody(batch);
        }
    }

    private void drawBody(SpriteBatch batch) {
        if (helmetAnimation.isPlaying()) {
            helmetAnimation.draw(batch, x, y, width, height, facingLeft);
            return;
        }

        if (whiskyAnimation.isPlaying()) {
            whiskyAnimation.draw(batch, x, y, width, height, facingLeft);
        } else if (tobaccoAnimation.isPlaying()) {
            tobaccoAnimation.draw(batch, x, y, width, height, facingLeft);
        } else if (mateAnimation.isPlaying()) {
            mateAnimation.draw(batch, x, y, width, height, facingLeft);
        } else if (fullHealAnimation.isPlaying()) {
            fullHealAnimation.draw(batch, x, y, width, height, facingLeft);
        } else if (easterEggGoku.isActive()) {
            easterEggGoku.draw(batch, x, y, facingLeft);
        } else {
            super.draw(batch);
        }

        if (helmetBonus.isActive()) {
            drawWornHelmet(batch);
        }
    }

    private void drawWornHelmet(SpriteBatch batch) {
        float helmetWidth = 29f;
        float helmetHeight = 18f;
        float headOffset = mateAnimation.isPlaying() && !tobaccoAnimation.isPlaying() ? 5f : 2f;
        float helmetCenter = facingLeft ? x + headOffset : x - headOffset;
        float helmetX = facingLeft ? helmetCenter + helmetWidth / 2f : helmetCenter - helmetWidth / 2f;
        batch.draw(wornHelmet, helmetX, y - height + 2f,
            facingLeft ? -helmetWidth : helmetWidth, helmetHeight);
    }

    @Override
    public void drawHealthBar(SpriteBatch batch, Texture pixel) {
        LIFE_HUGO_BARRE.draw(batch, pixel, getLife(), x, y - height - 10f);
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
        float rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        return new Projectile(bulletSprite, x, getCenterY(), dx, dy, BULLET_SPEED, damage,
            attackRange.getValue(), BULLET_SIZE, BULLET_SIZE, BULLET_SPRITE_WIDTH, BULLET_SPRITE_HEIGHT, rotation);
    }

    public Stat getFireDelay() {
        return fireDelay;
    }

    public void consumePendingUpgrade() {
        if (pendingUpgrades > 0) {    
            pendingUpgrades--;          
        }
    }

    public void registerKill(float xp) {
        addXp(xp); 
        kills++;
    }

    public int getKills() {
        return kills;
    }
}
