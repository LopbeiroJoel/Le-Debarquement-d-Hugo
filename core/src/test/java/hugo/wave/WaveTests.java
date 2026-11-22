package hugo.wave;

import hugo.mob.xp.MobXp;

public final class WaveTests {
    public static void main(String[] args) {
        WaveManager manager = new WaveManager();
        check(manager.getWave() == 1 && manager.getCycle() == 1, "start at wave one");
        check(!manager.update(24f), "no early wave");
        check(manager.update(1f), "next wave after 25 seconds");
        check(manager.getWave() == 2, "wave two");
        check(!manager.update(0.1f), "do not spawn the same wave twice");

        // finish the first cycle
        for (int i = 0; i < 4; i++) {
            check(manager.update(25f), "advance one wave");
        }
        check(manager.getWave() == 1 && manager.getCycle() == 2, "restart at the next cycle");
        check(Math.abs(manager.getDamageMultiplier() - 1.2f) < 0.001f, "cycle damage multiplier");

        // keep the remaining time
        WaveManager delayed = new WaveManager();
        check(delayed.update(51f), "handle more than one elapsed wave");
        check(delayed.getWave() == 3, "two waves elapsed");
        check(delayed.update(24f) && delayed.getWave() == 4, "keep the extra second");

        check(MobXp.rewardFor(1, 1) == 10, "first mob XP");
        check(MobXp.rewardFor(5, 1) == 250, "boss XP");
        check(MobXp.rewardFor(5, 2) == 300, "XP increases with the cycle");
        System.out.println("WaveTests: all checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
