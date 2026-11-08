package hugo.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Projectile implements Entity {

    private final TextureRegion sprite;
    private float x, y;
    private final float dirX, dirY;
    private final float speed;
    private final float damage;
    private final float maxRange;
    private final float width, height;
    private float traveled;
    private boolean active;
    private final Rectangle hitbox = new Rectangle();

    public Projectile(TextureRegion sprite, float x, float y, float dirX, float dirY, float speed, float damage, float maxRange, float width, float height) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
        this.damage = damage;
        this.maxRange = maxRange;
        this.width = width;
        this.height = height;
        this.traveled = 0;
        this.active = true;
    }

    @Override
    public void update(float deltaTime) {
        if (!active) {
            return;
        }

        float distance = speed * deltaTime;
        x += dirX * distance;
        y += dirY * distance;
        traveled += distance;

        if (traveled >= maxRange) {
            active = false;
        }

        hitbox.set(x - width / 2f, y - height / 2f, width, height);
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!active) {
            return;
        }
        batch.draw(sprite, x - width / 2f, y - height / 2f, width, height);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public void destroy() {
        active = false;
    }

}
