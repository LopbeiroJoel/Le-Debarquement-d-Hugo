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

        System.out.println("TestsArmes : tout passe");
    }
}