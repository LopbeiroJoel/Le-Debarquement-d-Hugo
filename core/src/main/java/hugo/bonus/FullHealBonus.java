package hugo.bonus;

import hugo.entity.Life;
import hugo.entity.Player;

public class FullHealBonus {
    public void apply(Player player) {
        Life life = player.getLife();
        float missingHealth = life.getMaxLife() - life.getCurrentLife();
        life.heal(missingHealth);
    }
}
