package hugo.weapon;

import java.util.List;

import hugo.entity.Projectile;

public final class TestsArmes {

    private static void check(boolean ok, String text) {
        if (!ok) throw new AssertionError(text);
    }

    public static void main(String[] args) {
        Weapon carbine = Weapons.carbine();

        // Hugo en (0, 0), cible en (300, 400) : la distance vaut 500
        List<Projectile> shots = carbine.fire(null, 0, 0, 300, 400, 8, 500);
        check(shots.size() == 1, "la carabine tire une balle");
        Projectile p = shots.get(0);
        check(p.getDamage() == 8, "degats transmis");
        check(p.getMaxRange() == 500, "portee transmise");
        check(Math.abs(p.getDirX() - 0.6) < 0.001f, "direction X");
        check(Math.abs(p.getDirY() - 0.8) < 0.001f, "direction Y");

        // Cible exactement sur Hugo : la direction ne doit pas etre NaN
        List<Projectile> same = carbine.fire(null, 10, 10, 10, 10, 8, 500);
        check(!Float.isNaN(same.get(0).getDirX()), "pas de NaN en X");
        check(!Float.isNaN(same.get(0).getDirY()), "pas de NaN en Y");

                // Pistolet-mitrailleur : faible, tres rapide, courte portee
        Weapon smg = Weapons.submachineGun();
        check(smg.getBaseDamage() == 3, "PM : degats");
        check(smg.getBaseFireDelay() == 0.2f, "PM : delai");
        check(smg.getBaseRange() == 350, "PM : portee");
        check(smg.fire(null, 0, 0, 100, 0, 3, 350).size() == 1, "PM : une balle");

        // Mitrailleuse : forte, cadence moyenne, longue portee
        Weapon machineGun = Weapons.machineGun();
        check(machineGun.getBaseDamage() == 12, "mitrailleuse : degats");
        check(machineGun.getBaseFireDelay() == 0.4f, "mitrailleuse : delai");
        check(machineGun.getBaseRange() == 600, "mitrailleuse : portee");
        check(machineGun.fire(null, 0, 0, 100, 0, 12, 600).size() == 1, "mitrailleuse : une balle");

        System.out.println("TestsArmes : tout passe");
    }
}