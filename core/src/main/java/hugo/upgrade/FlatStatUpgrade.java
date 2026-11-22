package hugo.upgrade;

import hugo.entity.Player;
import hugo.entity.Stat;

import java.util.function.Function;

public class FlatStatUpgrade extends Upgrade {

    private final Function<Player, Stat> statSelector; // select the player stat
    private final float amount;

    public FlatStatUpgrade(
            String name, String imageName, Function<Player, Stat> statSelector, float amount) {
        super(name, imageName);
        this.statSelector = statSelector;
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        Stat stat = statSelector.apply(player);
        stat.addBonus(amount);
    }
}
