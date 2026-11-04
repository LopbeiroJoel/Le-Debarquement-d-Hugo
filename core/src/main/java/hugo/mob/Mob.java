package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import hugo.entity.Creature;

public abstract class Mob extends Creature {

    private float attackCooldown = 0;
    private final Rectangle hitbox = new Rectangle();
    private final int hitboxSize;

    public Mob(Texture texture, float x, float y, int health, int speed,
               int width, int height, int hitboxSize) {
        super(texture, x, y, health, speed, width, height, 0, 5);

        this.hitboxSize = hitboxSize;
        updateHitbox();
    }

    public Mob(Texture texture, float x, float y, int health, int speed,
               int width, int height, int hitboxSize, int attackCooldown) {
        this(texture, x, y, health, speed, width, height, hitboxSize);
        this.attackCooldown = attackCooldown;
    }

    private void updateHitbox() {
        hitbox.set(
            x + (width - hitboxSize) / 2f, // f = float
            y + (height - hitboxSize) / 2f,
            hitboxSize,
            hitboxSize
        );
    }

    public Rectangle getHitbox() {
        updateHitbox();
        return hitbox;
    }

    public void follow(Rectangle hugoHitbox) {
        if (hugoHitbox == null || getLife().isDead()) {
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
}
