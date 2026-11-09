package hugo.upgrade;

import hugo.entity.Player;

public class MaxLifeUpgrade extends Upgrade {

    private final float factor;

    public MaxLifeUpgrade(String name, String imageName, float factor) {
        super(name, imageName);
        this.factor = factor;
    }

    @Override
    public void apply(Player player) {
        player.getLife().increaseMaxLife(factor);
    }
}