package hugo.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import hugo.entity.Projectile;

import java.util.ArrayList;
import java.util.List;

public class ShotgunWeapon extends Weapon {

    // pellets use a smaller sprite than rifle bullets
    private static final float SPRITE_WIDTH = 18f;
    private static final float SPRITE_HEIGHT = 14f;

    private final int pellets;
    private final float spread;

    public ShotgunWeapon(
            String name,
            String imageName,
            float baseDamage,
            float baseFireDelay,
            float baseRange,
            float bulletSpeed,
            float bulletSize,
            int pellets,
            float spread) {
        super(name, imageName, baseDamage, baseFireDelay, baseRange, bulletSpeed, bulletSize);
        this.pellets = pellets;
        this.spread = spread;
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
        float baseAngle = MathUtils.atan2(targetY - y, targetX - x) * MathUtils.radiansToDegrees;
        float step = spread / (pellets - 1);

        List<Projectile> shots = new ArrayList<>();
        for (int i = 0; i < pellets; i++) {
            float angle = baseAngle - spread / 2 + i * step;
            float dirX = MathUtils.cosDeg(angle);
            float dirY = MathUtils.sinDeg(angle);
            shots.add(
                    new Projectile(
                            sprite,
                            x,
                            y,
                            dirX,
                            dirY,
                            getBulletSpeed(),
                            damage,
                            range,
                            getBulletSize(),
                            getBulletSize(),
                            SPRITE_WIDTH,
                            SPRITE_HEIGHT,
                            angle));
        }
        return shots;
    }
}
