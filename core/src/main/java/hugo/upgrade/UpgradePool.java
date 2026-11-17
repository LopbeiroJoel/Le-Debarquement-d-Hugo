package hugo.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import hugo.entity.Player;
import hugo.weapon.Weapons;

public class UpgradePool {

    private final List<Upgrade> upgrades = new ArrayList<>();
    private final Random random = new Random();

    public UpgradePool() {
        upgrades.add(new StatUpgrade("+10 % de vitesse", "speed", player -> player.getSpeed(), 1.1f));
        upgrades.add(new StatUpgrade("+20 % de dégâts", "damage", player -> player.getAttackDamage(), 1.2f));
        upgrades.add(new StatUpgrade("+10 % de portée", "range", player -> player.getAttackRange(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de dégâts critiques", "crit-damage", player -> player.getCritMultiplier(), 1.1f));
        upgrades.add(new FlatStatUpgrade("+10 % de chance de critique", "crit-chance", player -> player.getCritChance(), 0.1f));
        upgrades.add(new StatUpgrade("-10 % de délai de tir", "fire-rate", player -> player.getFireDelay(), 0.9f));
        upgrades.add(new MaxLifeUpgrade("+20 % de vie maximum", "max-life", 1.2f));
        upgrades.add(new WeaponUpgrade("Pistolet-mitrailleur", "submachine-gun", () -> Weapons.submachineGun(), 0.7f));
        upgrades.add(new WeaponUpgrade("Mitrailleuse", "machine-gun", () -> Weapons.machineGun(), 0.3f));

    }

    public List<Upgrade> pickRandom(int count, Player player) {
        return pickRandom(upgrades, count, u -> u.isAvailableFor(player), random);
    }

    public static List<Upgrade> pickRandom(List<Upgrade> cards, int count,
            Predicate<Upgrade> available, Random rng) {
        // 1. le paquet : seulement les cartes disponibles et de poids positif
        List<Upgrade> deck = new ArrayList<>();
        for (Upgrade u : cards) {
            if (available.test(u) && u.getWeight() > 0) {
                deck.add(u);
            }
        }

        // 2. on tire une carte à la fois, jusqu'à en avoir "count" ou que le paquet soit vide
        List<Upgrade> picked = new ArrayList<>();
        while (picked.size() < count && !deck.isEmpty()) {
            float total = 0;
            for (Upgrade u : deck) {
                total += u.getWeight();
            }

            float ball = rng.nextFloat() * total;   // la bille sur la règle
            Upgrade chosen = deck.get(deck.size() - 1);
            for (Upgrade u : deck) {
                if (ball < u.getWeight()) {
                    chosen = u;
                    break;
                }
                ball -= u.getWeight();
            }

            picked.add(chosen);
            deck.remove(chosen);   // la carte tirée sort du paquet
        }
        return picked;
    }
}
