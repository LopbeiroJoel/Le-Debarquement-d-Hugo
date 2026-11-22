package hugo.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import hugo.entity.Projectile;

import java.util.ArrayList;
import java.util.List;

public class SingleShotWeapon extends Weapon {

    // copper bullet sprite size
    private static final float SPRITE_WIDTH = 24f;
    private static final float SPRITE_HEIGHT = 9f;

    public SingleShotWeapon(
            String name,
            String imageName,
            float baseDamage,
            float baseFireDelay,
            float baseRange,
            float bulletSpeed,
            float bulletSize) {
        super(name, imageName, baseDamage, baseFireDelay, baseRange, bulletSpeed, bulletSize);
    }

    @Override
    public List<Projectile> fire(
            TextureRegion sprite,
            float x,
            float y,
            float targetX,
            float targetY,
            float damage,
            float range) {
        float dx = targetX - x;
        float dy = targetY - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            dx = 1;
            dy = 0;
        } else {
            dx /= length;
            dy /= length;
        }
        float rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        List<Projectile> shots = new ArrayList<>();
        shots.add(
                new Projectile(
                        sprite,
                        x,
                        y,
                        dx,
                        dy,
                        getBulletSpeed(),
                        damage,
                        range,
                        getBulletSize(),
                        getBulletSize(),
                        SPRITE_WIDTH,
                        SPRITE_HEIGHT,
                        rotation));
        return shots;
    }
}
