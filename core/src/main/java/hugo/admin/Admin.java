package hugo.admin;

import hugo.mob.spawn.MobSpawner;

public final class Admin {
    // supprime tous les mobs
    public void killAllMobs(MobSpawner spawner) {
        spawner.getMobs().clear();
    }
}
