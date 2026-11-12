package hugo.bonus;

import hugo.entity.Life;
import hugo.entity.Player;

public final class TobaccoBonus {
    private static final float DURATION = 30f;
    private static final float DAMAGE_MULTIPLIER = 3f;
    private static final float FIRE_DELAY_MULTIPLIER = 0.15f;

    private float remainingTime;

    public void activate(Player player) {
        Life life = player.getLife();
        life.takeDamage(life.getCurrentLife() / 2f);

        if (remainingTime <= 0f) {
            player.getAttackDamage().addMultiplier(DAMAGE_MULTIPLIER);
            player.getFireDelay().addMultiplier(FIRE_DELAY_MULTIPLIER);
        }

        remainingTime = DURATION;
    }

    public void update(Player player, float delta) {
        if (remainingTime <= 0f) {
            return;
        }

        remainingTime -= delta;

        if (remainingTime <= 0f) {
            player.getAttackDamage().removeMultiplier(DAMAGE_MULTIPLIER);
            player.getFireDelay().removeMultiplier(FIRE_DELAY_MULTIPLIER);
        }
    }
}
