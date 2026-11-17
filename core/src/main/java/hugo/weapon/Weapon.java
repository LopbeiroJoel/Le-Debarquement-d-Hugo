package hugo.weapon;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hugo.entity.Projectile;

public abstract class Weapon {

    private final String name;
    private final String imageName;
    private final float baseDamage;
    private final float baseFireDelay;
    private final float baseRange;
    private final float bulletSpeed;
    private final float bulletSize;

    public Weapon(String name, String imageName, float baseDamage, float baseFireDelay,
            float baseRange, float bulletSpeed, float bulletSize) {
        this.name = name;
        this.imageName = imageName;
        this.baseDamage = baseDamage;
        this.baseFireDelay = baseFireDelay;
        this.baseRange = baseRange;
        this.bulletSpeed = bulletSpeed;
        this.bulletSize = bulletSize;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getBaseFireDelay() {
        return baseFireDelay;
    }

    public float getBaseRange() {
        return baseRange;
    }

    protected float getBulletSpeed() {
        return bulletSpeed;
    }

    protected float getBulletSize() {
        return bulletSize;
    }

    public abstract List<Projectile> fire(TextureRegion sprite, float x, float y,
            float targetX, float targetY, float damage, float range);

}
