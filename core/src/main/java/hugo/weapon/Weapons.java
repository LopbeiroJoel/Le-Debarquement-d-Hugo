package hugo.weapon;

public final class Weapons {

    private Weapons() {
    }

    public static SingleShotWeapon carbine() {
        return new SingleShotWeapon("Carabine", "carbine", 8, 1.0f, 500, 1000.0f, 8.0f);
    }
}