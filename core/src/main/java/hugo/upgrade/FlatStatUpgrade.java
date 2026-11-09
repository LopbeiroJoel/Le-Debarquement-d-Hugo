package hugo.upgrade;

import java.util.function.Function;

import hugo.entity.Player;
import hugo.entity.Stat;

public class FlatStatUpgrade extends Upgrade {

    private final Function<Player, Stat> statSelector;   // comment trouver la stat dans le joueur
    private final float amount;                          

    public FlatStatUpgrade(String name, Function<Player, Stat> statSelector, float amount) {
        super(name);                                   
        this.statSelector = statSelector;
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        Stat stat = statSelector.apply(player);          
        stat.addBonus(amount);                             
    }
}