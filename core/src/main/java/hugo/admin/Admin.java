package hugo.admin;

import hugo.mob.spawn.MobSpawner;

public final class Admin {
    // clear all mobs
    public void killAllMobs(MobSpawner spawner) {
        spawner.getMobs().clear();
    }
}
