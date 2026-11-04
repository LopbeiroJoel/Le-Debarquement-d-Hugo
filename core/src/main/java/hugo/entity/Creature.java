package hugo.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Creature implements Entity {

    protected Texture sprite;
    protected float x;
    protected float y;
    protected Life life;
    protected float speed;
    protected float width, height;
    protected float attackRange;
    protected float attackDamage;
    protected float dirX, dirY;
    private final Rectangle hitbox = new Rectangle();
    private final float hitboxSize;

    public Creature(Texture sprite, float x, float y, float maxLife, float speed, float width, float height, float attackRange, float attackDamage, float hitboxSize) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.life = new Life(maxLife);
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.attackRange = attackRange;
        this.attackDamage = attackDamage;
        this.hitboxSize = hitboxSize;

    }

    public Life getLife() {
        return life;
    }

    public void setDirection(float dirX, float dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
        if (dirX >= 1 || dirY >= 1 || dirX <= -1 || dirY <= -1) {
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            this.dirX /= length;
            this.dirY /= length;
        }
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

    @Override
    public void update(float deltaTime) {
        this.x += dirX * speed * deltaTime;
        this.y += dirY * speed * deltaTime;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(sprite, x, y, width, height);
    }
}
