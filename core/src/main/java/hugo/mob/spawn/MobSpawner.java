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
    private final List<Mob> mobs = new ArrayList<>();
    private final Map<String, Texture> textures = new HashMap<>();

    public List<Mob> getMobs() {
        return mobs;
    }

    public void spawnWave(WaveManager waveManager, CarteJeu map, float hugoX, float hugoY) {
        Vector2 position = findSpawnPosition(map, hugoX, hugoY);
        float x = position.x;
        float y = position.y;
        Mob mob = switch (waveManager.getWave()) {
            case 2 -> new MobLvl2(texture("german-shepherd-walk.png"),
                texture("german-shepherd-bite.png"), x, y);
            case 3 -> new MobLvl3(texture("machete-walk.png"), texture("machete-attack.png"), x, y);
            case 4 -> new MobLvl4(texture("veteran-walk.png"), texture("veteran-fire.png"), x, y);
            case 5 -> new MobLvl5(texture("boss-walk.png"), texture("boss-fire.png"),
                texture("boss-knife.png"), x, y);
            default -> new MobLvl1(texture("soldier-level-1.png"), x, y);
        };

        mob.getAttackDamage().addMultiplier(waveManager.getDamageMultiplier());
        mobs.add(mob);
    }

    public void updateWave(WaveManager waveManager, float delta, CarteJeu map, float hugoX, float hugoY) {
        if (waveManager.update(delta)) {
            spawnWave(waveManager, map, hugoX, hugoY);
        }
    }

    private Vector2 findSpawnPosition(CarteJeu map, float x, float y) {
        Vector2 bestPosition = new Vector2(x, y);
        float bestDistance = 0;
        float[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (float[] direction : directions) {
            Vector2 position = map.move(x, y, direction[0] * SPAWN_DISTANCE, direction[1] * SPAWN_DISTANCE);
            float distance = position.dst2(x, y);
            if (distance >= SPAWN_DISTANCE * SPAWN_DISTANCE) {
                return position;
            }
            if (distance > bestDistance) {
                bestPosition = position;
                bestDistance = distance;
            }
        }
        return bestPosition;
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
