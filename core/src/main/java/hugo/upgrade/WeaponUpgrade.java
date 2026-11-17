package hugo.upgrade;

import java.util.function.Supplier;

import hugo.entity.Player;
import hugo.weapon.Weapon;

public class WeaponUpgrade extends Upgrade {

    private final Supplier<Weapon> weapon;

    public WeaponUpgrade(String name, String imageName, Supplier<Weapon> weapon, float weight) {
        super(name, imageName, weight);
        this.weapon = weapon;
    }

    @Override
    public void apply(Player player) {
        player.equipWeapon(weapon.get());
    }

    @Override
    public boolean isAvailableFor(Player player) {
        return !player.getWeapon().getImageName().equals(weapon.get().getImageName());
    }
}