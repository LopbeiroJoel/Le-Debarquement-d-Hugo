package hugo.weapon;

public final class Weapons {

    private Weapons() {}

    public static Weapon carbine() {
        return new SingleShotWeapon("Carbine", "carbine", 8, 1.0f, 500, 1000.0f, 8.0f);
    }

    public static Weapon submachineGun() {
        return new SingleShotWeapon(
                "Submachine gun", "submachine-gun", 3, 0.2f, 350, 1100.0f, 8.0f);
    }

    public static Weapon machineGun() {
        return new SingleShotWeapon("Machine gun", "machine-gun", 12, 0.4f, 600, 1200.0f, 10.0f);
    }

    public static Weapon shotgun() {
        return new ShotgunWeapon("Shotgun", "shotgun", 6, 1.4f, 300, 900.0f, 6.0f, 5, 40.0f);
    }
}
