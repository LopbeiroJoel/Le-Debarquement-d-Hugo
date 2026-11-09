package hugo.upgrade;

import java.util.function.Function;

import hugo.entity.Player;
import hugo.entity.Stat;

public class StatUpgrade extends Upgrade {

    private final Function<Player, Stat> statSelector;   // comment trouver la stat dans le joueur
    private final float factor;                          

    public StatUpgrade(String name, Function<Player, Stat> statSelector, float factor) {
        super(name);                                   
        this.statSelector = statSelector;
        this.factor = factor;
    }

    @Override
    public void apply(Player player) {
        Stat stat = statSelector.apply(player);          
        stat.addMultiplier(factor);                             
    }
}