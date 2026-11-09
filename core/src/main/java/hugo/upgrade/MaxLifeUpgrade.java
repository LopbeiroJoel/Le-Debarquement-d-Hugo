package hugo.upgrade;

import hugo.entity.Player;

public class MaxLifeUpgrade extends Upgrade {

    private final float factor;

    public MaxLifeUpgrade(String name, float factor) {
        super(name);                     
        this.factor = factor;
    }

    @Override
    public void apply(Player player) {
        player.getLife().increaseMaxLife(factor);
    }
}