package hugo.mob.spawn;

import com.badlogic.gdx.graphics.Texture;
import hugo.mob.Mob;
import hugo.mob.MobLvl1;
import hugo.mob.MobLvl2;
import hugo.wave.WaveManager;

import java.util.ArrayList;
import java.util.List;

public class MobSpawner {
    private final List<Mob> mobs = new ArrayList<>();

    public List<Mob> getMobs() {
        return mobs;
    }

    public void spawnLevelOne(Texture texture, float x, float y,
                              float damageMultiplier) {
        MobLvl1 mob = new MobLvl1(texture, x, y);
        mob.getAttackDamage().addMultiplier(damageMultiplier);
        mobs.add(mob);
    }

    public void updateWave(WaveManager waveManager, float delta, Texture soldierTexture,
                           Texture dogWalkTexture, Texture dogBiteTexture, float x, float y) {
        if (!waveManager.update(delta)) {
            return;
        }

        if (waveManager.getWave() == 2 && dogWalkTexture != null && dogBiteTexture != null) {
            mobs.add(new MobLvl2(dogWalkTexture, dogBiteTexture, x, y));
        } else if (soldierTexture != null) {
            spawnLevelOne(soldierTexture, x, y, waveManager.getDamageMultiplier());
        }
    }
}
