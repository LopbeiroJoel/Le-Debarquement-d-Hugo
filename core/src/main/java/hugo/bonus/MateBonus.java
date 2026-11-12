package hugo.bonus;

import hugo.entity.Player;

public class MateBonus {
    private static final float DURATION = 30f;
    private static final float SPEED_MULTIPLIER = 2.25f;
    private static final float FIRE_DELAY_MULTIPLIER = 0.175f;

    private float remainingTime;

    public void activate(Player player) {
        if (remainingTime <= 0f) {
            player.getSpeed().addMultiplier(SPEED_MULTIPLIER);
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
            player.getSpeed().removeMultiplier(SPEED_MULTIPLIER);
            player.getFireDelay().removeMultiplier(FIRE_DELAY_MULTIPLIER);
        }
    }
}