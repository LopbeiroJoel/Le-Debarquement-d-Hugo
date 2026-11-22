package hugo.weapon;

import com.badlogic.gdx.math.MathUtils;

import hugo.entity.Projectile;

import java.util.List;

public final class WeaponTests {

    private static void check(boolean ok, String text) {
        if (!ok) {
            throw new AssertionError(text);
        }
    }

    public static void main(String[] args) {
        Weapon carbine = Weapons.carbine();

        // player at (0, 0), target at (300, 400), distance is 500
        List<Projectile> shots = carbine.fire(null, 0, 0, 300, 400, 8, 500);
        check(shots.size() == 1, "carbine fires one bullet");
        Projectile p = shots.get(0);
        check(p.getDamage() == 8, "damage passed to projectile");
        check(p.getMaxRange() == 500, "range passed to projectile");
        check(Math.abs(p.getDirX() - 0.6) < 0.001f, "direction X");
        check(Math.abs(p.getDirY() - 0.8) < 0.001f, "direction Y");

        // target at the player position, direction must stay finite
        List<Projectile> same = carbine.fire(null, 10, 10, 10, 10, 8, 500);
        check(!Float.isNaN(same.get(0).getDirX()), "finite X direction");
        check(!Float.isNaN(same.get(0).getDirY()), "finite Y direction");

        // submachine gun, low damage and high fire rate
        Weapon smg = Weapons.submachineGun();
        check(smg.getBaseDamage() == 3, "submachine gun damage");
        check(smg.getBaseFireDelay() == 0.2f, "submachine gun delay");
        check(smg.getBaseRange() == 350, "submachine gun range");
        check(smg.fire(null, 0, 0, 100, 0, 3, 350).size() == 1, "submachine gun fires one bullet");

        // machine gun, high damage and long range
        Weapon machineGun = Weapons.machineGun();
        check(machineGun.getBaseDamage() == 12, "machine gun damage");
        check(machineGun.getBaseFireDelay() == 0.4f, "machine gun delay");
        check(machineGun.getBaseRange() == 600, "machine gun range");
        check(
                machineGun.fire(null, 0, 0, 100, 0, 12, 600).size() == 1,
                "machine gun fires one bullet");

        // shotgun, five pellets over forty degrees
        Weapon shotgun = Weapons.shotgun();
        check(shotgun.getBaseDamage() == 6, "shotgun damage per pellet");
        check(shotgun.getBaseFireDelay() == 1.4f, "shotgun delay");
        check(shotgun.getBaseRange() == 300, "shotgun range");

        // target to the right, zero degrees
        List<Projectile> pellets = shotgun.fire(null, 0, 0, 100, 0, 6, 260);
        check(pellets.size() == 5, "shotgun fires five pellets");
        float[] expected = {-20, -10, 0, 10, 20};
        for (int i = 0; i < 5; i++) {
            Projectile pellet = pellets.get(i);
            float angle =
                    MathUtils.atan2(pellet.getDirY(), pellet.getDirX())
                            * MathUtils.radiansToDegrees;
            check(
                    Math.abs(angle - expected[i]) < 0.5f,
                    "pellet " + i + " at " + expected[i] + " degrees, expected " + angle);
            check(pellet.getDamage() == 6, "each pellet uses the supplied damage");
        }
        // target below the player, ninety degrees
        List<Projectile> down = shotgun.fire(null, 0, 0, 0, 100, 6, 260);
        Projectile middle = down.get(2);
        float middleAngle =
                MathUtils.atan2(middle.getDirY(), middle.getDirX()) * MathUtils.radiansToDegrees;
        check(
                Math.abs(middleAngle - 90) < 0.5f,
                "middle pellet points to target, actual angle " + middleAngle);

        System.out.println("WeaponTests: all checks passed");
    }
}
