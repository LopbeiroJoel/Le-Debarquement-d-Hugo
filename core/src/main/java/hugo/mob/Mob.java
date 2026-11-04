package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import hugo.entity.Creature;

public abstract class Mob extends Creature {

    private float attackCooldown = 0;
    private float attackInterval = 1;

    public Mob(Texture texture, float x, float y, int health, int speed,
               int width, int height, int hitboxSize) {
        super(texture, x, y, health, speed, width, height, 0, 5, hitboxSize);

    }

    public Mob(Texture texture, float x, float y, int health, int speed,
               int width, int height, int hitboxSize, int attackInterval) {
        this(texture, x, y, health, speed, width, height, hitboxSize);
        this.attackInterval = Math.max(0, attackInterval);
    }

    public void follow(Rectangle hugoHitbox) {
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
        if (target == null || target == this || life.isDead()
                || target.getLife().isDead() || attackCooldown > 0) {
            return false;
        }

        if (!getHitbox().overlaps(target.getHitbox())) {
            return false;
        }

        target.getLife().takeDamage(attackDamage);
        attackCooldown = attackInterval;
        onAttack();
        return true;
    }

    protected void onAttack() {
    }

    public float getFeetY() {
        return y + height;
    }

    public void draw(SpriteBatch batch) {
        if (sprite == null) {
            return;
        }

        batch.draw(
            sprite,
            x,
            y,
            width,
            height,
            0,
            0,
            sprite.getWidth(),
            sprite.getHeight(),
            false,
            true
        );
    }
}
