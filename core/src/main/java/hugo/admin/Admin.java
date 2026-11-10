package hugo.admin;

import hugo.mob.spawn.MobSpawner;

public final class Admin {
    public void killAllMobs(MobSpawner spawner) {
        spawner.getMobs().clear();
    }
}
