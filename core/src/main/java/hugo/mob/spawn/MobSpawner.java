package hugo.mob.spawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import hugo.map.CarteJeu;
import hugo.mob.Mob;
import hugo.mob.MobLvl1;
import hugo.mob.MobLvl2;
import hugo.mob.MobLvl3;
import hugo.mob.MobLvl4;
import hugo.mob.MobLvl5;
import hugo.wave.WaveManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobSpawner implements Disposable {
    private static final float SPAWN_DISTANCE = 220f;
    private static final float MIN_MOB_DISTANCE = 40f;

    private static final int[][] WAVE_COUNTS = {
        {10,  0,  0,  0, 0},
        {10, 15,  0,  0, 0},
        {10, 10, 20,  0, 0},
        {10, 10, 10, 20, 0},
        { 9, 10, 15, 25, 1}
    };

    private final List<Mob> mobs = new ArrayList<>();
    private final Map<String, Texture> textures = new HashMap<>();

    public List<Mob> getMobs() {
        return mobs;
    }

    public boolean hasRoom(Mob movingMob, float nextX, float nextY) {
        for (Mob other : mobs) {
            if (other == movingMob || other.getLife().isDead()) {
                continue;
            }

            float dx = nextX - other.getX();
            float dy = nextY - other.getY();

            if (dx * dx + dy * dy < MIN_MOB_DISTANCE * MIN_MOB_DISTANCE) {
                return false;
            }
        }

        return true;
    }

    public void spawnWave(WaveManager waveManager, CarteJeu map, float hugoX, float hugoY) {
        int[] counts = WAVE_COUNTS[waveManager.getWave() - 1];

        int total = 0;
        for (int count : counts) {
            total += count;
        }

        int mobIndex = 0;
        double startAngle = Math.random() * 2 * Math.PI;
        float distance = Math.max(SPAWN_DISTANCE, total * 7f);

        for (int typeIndex = 0; typeIndex < counts.length; typeIndex++) {
            int level = typeIndex + 1;

            for (int i = 0; i < counts[typeIndex]; i++) {
                double angle = startAngle + 2 * Math.PI * mobIndex / total;
                float offsetX = (float) Math.cos(angle) * distance;
                float offsetY = (float) Math.sin(angle) * distance;
                Vector2 position = map.move(hugoX, hugoY, offsetX, offsetY);

                Mob mob = createMob(level, position.x, position.y);
                mob.getAttackDamage().addMultiplier(waveManager.getDamageMultiplier());
                mobs.add(mob);
                mobIndex++;
            }
        }

        System.out.println("Wave " + waveManager.getWave() + ": " + mobIndex + " mobs spawned");
    }

    private Mob createMob(int level, float x, float y) {
        return switch (level) {
            case 2 -> new MobLvl2(
                texture("german-shepherd-walk.png"),
                texture("german-shepherd-bite.png"), x, y
            );
            case 3 -> new MobLvl3(
                texture("machete-walk.png"),
                texture("machete-attack.png"), x, y
            );
            case 4 -> new MobLvl4(
                texture("veteran-walk.png"),
                texture("veteran-fire.png"), x, y
            );
            case 5 -> new MobLvl5(
                texture("boss-walk.png"),
                texture("boss-fire.png"),
                texture("boss-knife.png"), x, y
            );
            default -> new MobLvl1(texture("soldier-level-1.png"), x, y);
        };
    }

    public void updateWave(WaveManager waveManager, float delta, CarteJeu map, float hugoX, float hugoY) {
        if (waveManager.update(delta)) {
            spawnWave(waveManager, map, hugoX, hugoY);
        }
    }

    private Texture texture(String name) {
        return textures.computeIfAbsent(name, this::loadTexture);
    }

    private Texture loadTexture(String name) {
        Texture texture = new Texture(Gdx.files.internal("mobs/" + name), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        mobs.clear();
    }
}
