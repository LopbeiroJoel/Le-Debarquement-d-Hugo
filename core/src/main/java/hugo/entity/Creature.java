package hugo.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import hugo.map.CarteJeu;

public abstract class Creature implements Entity {

    protected TextureRegion sprite;
    protected float x;
    protected float y;
    protected Life life;
    protected Stat speed;
    protected float width, height;
    protected Stat attackRange;
    protected Stat attackDamage;
    protected float dirX, dirY;
    private final Rectangle hitbox = new Rectangle();
    private final float hitboxSize;
    protected CarteJeu map;
    protected boolean facingLeft = false;  

    public Creature(TextureRegion sprite, float x, float y, float maxLife, float baseSpeed, float width, float height, float attackRange, float attackDamage, float hitboxSize) {
        this.sprite = new TextureRegion(sprite);
        this.sprite.flip(false, true);
        this.x = x;
        this.y = y;
        this.life = new Life(maxLife);
        this.speed = new Stat(baseSpeed);
        this.width = width;
        this.height = height;
        this.attackRange = new Stat(attackRange);
        this.attackDamage = new Stat(attackDamage);
        this.hitboxSize = hitboxSize;

    }

    public Life getLife() {
        return life;
    }

    public Stat getSpeed() {
        return speed;
    }

    public Stat getAttackRange() {
        return attackRange;
    }

    public Stat getAttackDamage() {
        return attackDamage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
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
                x - hitboxSize / 2f,
                getCenterY() - hitboxSize / 2f,
                hitboxSize,
                hitboxSize
        );
    }

    public Rectangle getHitbox() {
        updateHitbox();
        return hitbox;
    }

    public float getCenterY() {
        return y - height / 2f;
    }

    @Override
    public void update(float deltaTime) {
        float dx = dirX * speed.getValue() * deltaTime;
        float dy = dirY * speed.getValue() * deltaTime;

        if (dx < 0) {
            facingLeft = true;
        } else if (dx > 0) {
            facingLeft = false;
        }
        if (map == null) {
            x += dx;
            y += dy;
            return;
        }

        Vector2 newPosition = map.move(x, y, dx, dy);
        x = newPosition.x;
        y = newPosition.y;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (facingLeft) {
            batch.draw(sprite, x + width / 2f, y - height, -width, height);
        } else {
            batch.draw(sprite, x - width / 2f, y - height, width, height);
        }
    }

    public void setMap(CarteJeu map) {
        this.map = map;
    }
}
