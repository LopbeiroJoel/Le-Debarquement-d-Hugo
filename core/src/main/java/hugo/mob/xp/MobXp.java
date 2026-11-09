package hugo.mob.xp;

public final class MobXp {
    private static final int[] BASE_REWARDS = {10, 20, 40, 80, 250};
    private static final double CYCLE_MULTIPLIER = 1.2;

    private MobXp() {
    }

    public static int rewardFor(int level, int cycle) {
        int baseReward = BASE_REWARDS[level - 1];
        double multiplier = Math.pow(CYCLE_MULTIPLIER, cycle - 1);
        return Math.round((float) (baseReward * multiplier));
    }
}
