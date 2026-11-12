package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import hugo.entity.Creature;
import hugo.entity.Player;
import hugo.entity.Projectile;
import hugo.map.CarteJeu;
import hugo.mob.spawn.MobSpawner;

import java.util.List;

public abstract class Mob extends Creature {

    private static final int[] MOVEMENT_ANGLES = {0, 45, -45, 90, -90};

    private float attackCooldown = 0;
    private float attackInterval = 1;
    private int xpReward;
    private TextureRegion projectileSprite;

    public Mob(Texture texture, float x, float y, int health, int speed,
               int width, int height, int hitboxSize) {
        this(texture, x, y, health, speed, width, height, hitboxSize, 5f);
    }

    protected Mob(Texture texture, float x, float y, int health, int speed,
                  int width, int height, int hitboxSize, float damage) {
        super(new TextureRegion(texture), x, y, health, speed, width, height, 0, damage, hitboxSize);
    }

    public Mob(Texture texture, float x, float y, int health, int speed,
               int width, int height, int hitboxSize, int attackInterval) {
        this(texture, x, y, health, speed, width, height, hitboxSize);
        this.attackInterval = Math.max(0, attackInterval);
    }

    public void updateAgainst(Creature target, CarteJeu map, float deltaTime, MobSpawner spawner) {
        if (target instanceof Player player && player.hasParachuteStrike()) {
            spawner.defeat(this);
            return;
        }

        setMap(map);
        follow(target == null ? null : target.getHitbox());

        chooseMovement(map, deltaTime, spawner);
        update(deltaTime);
    }

    private void chooseMovement(CarteJeu map, float deltaTime, MobSpawner spawner) {
        if (dirX == 0 && dirY == 0) {
            return;
        }

        float step = getSpeed().getValue() * deltaTime;
        Vector2 direction = new Vector2();
        for (int angle : MOVEMENT_ANGLES) {
            direction.set(dirX, dirY).rotateDeg(angle);
            Vector2 next = map.move(x, y, direction.x * step, direction.y * step);
            if ((next.x != x || next.y != y) && spawner.hasRoom(this, next.x, next.y)) {
                setDirection(direction.x, direction.y);
                return;
            }
        }
        setDirection(0, 0);
    }

    public void follow(Rectangle hugoHitbox) { //bah follow quoi
        if (hugoHitbox == null || life.isDead()) {
            setDirection(0, 0); // pas de Hugo ou mob mort
            return;
        }

        Rectangle mobHitbox = getHitbox();

        if (mobHitbox.overlaps(hugoHitbox)) {
            setDirection(0, 0); // touche Hugo, on bouge plus
            return;
        }

        float mobCenterX = mobHitbox.x + mobHitbox.width / 2f;
        float mobCenterY = mobHitbox.y + mobHitbox.height / 2f;
        float hugoCenterX = hugoHitbox.x + hugoHitbox.width / 2f;
        float hugoCenterY = hugoHitbox.y + hugoHitbox.height / 2f;

        float distanceX = hugoCenterX - mobCenterX;
        float distanceY = hugoCenterY - mobCenterY;

        float distance = (float) Math.sqrt(
            distanceX * distanceX + distanceY * distanceY
        );

        if (distance == 0) {
            setDirection(0, 0);
            return;
        }

        setDirection(distanceX / distance, distanceY / distance);
    }
    public void update(float deltaTime) {
        if (deltaTime <= 0) {
            return;
        }

        if (life.isDead()) {
            setDirection(0, 0);
        }

        super.update(deltaTime);
        attackCooldown = Math.max(0, attackCooldown - deltaTime);
    }

    public boolean attack(Creature target) {
        if (target == null || target.getLife().isDead() || !startMeleeAttack(target)) {
            return false;
        }

        target.takeDamage(getAttackDamage().getValue());
        return true;
    }

    public void attackTarget(Creature target, TextureRegion bullet, List<Projectile> projectiles) {
        startMeleeAttack(target);
    }

    protected boolean startMeleeAttack(Creature target) {
        if (!canAttack(target) || !getHitbox().overlaps(target.getHitbox())) {
            return false;
        }

        attackCooldown = attackInterval;
        onAttack();
        return true;
    }

    protected boolean shootAt(Creature target, TextureRegion bullet, List<Projectile> projectiles, float range) {
        if (!canAttack(target)) {
            return false;
        }

        float startY = getCenterY();
        float dx = target.getX() - x;
        float dy = target.getCenterY() - startY;
        float distance = (float) Math.hypot(dx, dy);
        if (distance == 0 || distance > range || map.blocksShot(x, startY, target.getX(), target.getCenterY())) {
            return false;
        }

        Projectile projectile;
        if (projectileSprite == null) {
            projectile = new Projectile(bullet, x, startY, dx / distance, dy / distance,
                360f, 0f, range, 8f, 8f);
        } else {
            float rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            projectile = new Projectile(projectileSprite, x, startY, dx / distance, dy / distance,
                360f, 0f, range, 8f, 8f, 24f, 9f, rotation);
        }
        projectile.setMap(map);
        projectiles.add(projectile);
        attackCooldown = attackInterval;
        return true;
    }

    private boolean canAttack(Creature target) {
        return target != null && target != this && !life.isDead() && attackCooldown <= 0;
    }

    protected void onAttack() {
    }

    public void setProjectileSprite(TextureRegion projectileSprite) {
        this.projectileSprite = projectileSprite;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public float getFeetY() {
        return y;
    }
}
