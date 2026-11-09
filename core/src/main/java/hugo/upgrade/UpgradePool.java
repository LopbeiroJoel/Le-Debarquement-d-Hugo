package hugo.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradePool {

    private final List<Upgrade> upgrades = new ArrayList<>();   

    public UpgradePool() {
        upgrades.add(new StatUpgrade("+10 % de vitesse", "speed", player -> player.getSpeed(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de dégâts", "damage", player -> player.getAttackDamage(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de portée", "range", player -> player.getAttackRange(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de dégâts critiques", "crit-damage", player -> player.getCritMultiplier(), 1.1f));
        upgrades.add(new FlatStatUpgrade("+10 % de chance de critique", "crit-chance", player -> player.getCritChance(), 0.1f));
        upgrades.add(new StatUpgrade("-10 % de délai de tir", "fire-rate", player -> player.getFireDelay(), 0.9f));
        upgrades.add(new MaxLifeUpgrade("+20 % de vie maximum", "max-life", 1.2f));
        
    }

    public List<Upgrade> pickRandom(int count) {
        List<Upgrade> deck = new ArrayList<>(upgrades);   
        Collections.shuffle(deck);                         
        return deck.subList(0, Math.min(count, deck.size()));   
    }
}