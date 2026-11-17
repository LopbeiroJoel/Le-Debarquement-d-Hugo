package hugo.weapon;

public final class Weapons {

    private Weapons() {
    }

    public static Weapon carbine() {
        return new SingleShotWeapon("Carabine", "carbine", 8, 1.0f, 500, 1000.0f, 8.0f);
    }

    public static Weapon submachineGun() {
        return new SingleShotWeapon("Pistolet-mitrailleur", "submachine-gun", 3, 0.2f, 350, 1100.0f, 8.0f);
    }

    public static Weapon machineGun() {
        return new SingleShotWeapon("Mitrailleuse", "machine-gun", 12, 0.4f, 600, 1200.0f, 10.0f);
    }
}