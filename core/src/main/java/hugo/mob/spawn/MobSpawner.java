package hugo.mob.spawn;

import com.badlogic.gdx.graphics.Texture;
import hugo.mob.MobLvl1;
import hugo.wave.WaveManager;

public class MobSpawner {
    public MobLvl1 spawnLevelOne(Texture texture, float x, float y,
                                 float damageMultiplier) {
        MobLvl1 mob = new MobLvl1(texture, x, y);
        mob.getAttackDamage().addMultiplier(damageMultiplier);
        return mob;
    }

public MobLvl1 updateWave(WaveManager waveManager,float delta,MobLvl1 currentMob,Texture texture,float x,float y){
    boolean waveChanged = waveManager.update(delta);
    if (waveChanged&& texture!=null) {
        return spawnLevelOne(texture, x, y, waveManager.getDamageMultiplier());
    }
    return currentMob;
}
}
