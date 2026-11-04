package hugo.entite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Entite {

    protected Texture sprite;
    protected float x;
    protected float y;
    protected float life;
    protected float speed;
    protected float width, height;
    protected float attackRange;
    protected float attackDamage;
    protected float dirX, dirY;

    public Entite(Texture sprite, float x, float y, float life, float speed, float width, float height, float attackRange, float attackDamage) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.life = life;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.attackRange = attackRange;
        this.attackDamage = attackDamage;

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

    public void update(float deltaTime) {
        this.x += dirX * speed * deltaTime;
        this.y += dirY * speed * deltaTime;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(sprite, x, y, width, height);
    }
}
