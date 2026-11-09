package hugo.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradePool {

    private final List<Upgrade> upgrades = new ArrayList<>();   

    public UpgradePool() {
        upgrades.add(new StatUpgrade("+10 % de vitesse", player -> player.getSpeed(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de dégâts", player -> player.getAttackDamage(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de portée", player -> player.getAttackRange(), 1.1f));
        upgrades.add(new StatUpgrade("+10 % de dégâts critique", player -> player.getCritMultiplier(), 1.1f));
        upgrades.add(new FlatStatUpgrade("+10 % de chance de critique", player -> player.getCritChance(), 0.1f));
        upgrades.add(new StatUpgrade("-10 % de délai de tir", player -> player.getFireDelay(), 0.9f));
        
    }

    public List<Upgrade> pickRandom(int count) {
        List<Upgrade> deck = new ArrayList<>(upgrades);   
        Collections.shuffle(deck);                         
        return deck.subList(0, Math.min(count, deck.size()));   
    }
}