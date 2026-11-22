package hugo.mob.spawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import hugo.map.GameMap;
import hugo.mob.Mob;
import hugo.mob.MobLvl1;
import hugo.mob.MobLvl2;
import hugo.mob.MobLvl3;
import hugo.mob.MobLvl4;
import hugo.mob.MobLvl5;
import hugo.mob.xp.MobXp;
import hugo.wave.WaveManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobSpawner implements Disposable {
    private static final float SPAWN_DISTANCE = 750f;
    private static final float MIN_MOB_DISTANCE = 40f;

    // mobs per wave
    private static final int[][] WAVE_COUNTS = {
        {5, 0, 0, 0, 0},
        {5, 8, 0, 0, 0},
        {5, 5, 10, 0, 0},
        {5, 5, 5, 10, 0},
        {4, 5, 8, 12, 1}
    };

    private final List<Mob> mobs = new ArrayList<>();
    private final Map<String, Texture> textures = new HashMap<>();
    private TextureRegion projectileSprite;
    private boolean hasDefeatedMobs;

    public List<Mob> getMobs() {
        return mobs;
    }

    // space between mobs
    public boolean hasRoom(Mob movingMob, float nextX, float nextY) {
        for (Mob other : mobs) {
            if (other == movingMob || other.getLife().isDead()) {
                continue;
            }

            float dx = nextX - other.getX();
            float dy = nextY - other.getY();

            float nextDistance = dx * dx + dy * dy;
            if (nextDistance >= MIN_MOB_DISTANCE * MIN_MOB_DISTANCE) {
                continue;
            }
            if (movingMob != null) {
                float currentDx = movingMob.getX() - other.getX();
                float currentDy = movingMob.getY() - other.getY();
                if (nextDistance > currentDx * currentDx + currentDy * currentDy) {
                    continue;
                }
            }
            return false;
        }

        return true;
    }

    // spawn the wave
    public void spawnWave(WaveManager waveManager, GameMap map, float hugoX, float hugoY) {
        int[] counts = WAVE_COUNTS[waveManager.getWave() - 1];

        int total = 0;
        for (int count : counts) {
            total += count;
        }

        int mobIndex = 0;
        double fullCircle = 2 * Math.PI;
        double startAngle = Math.random() * fullCircle;
        float distance = Math.max(SPAWN_DISTANCE, total * 9f);

        for (int typeIndex = 0; typeIndex < counts.length; typeIndex++) {
            int level = typeIndex + 1;

            for (int i = 0; i < counts[typeIndex]; i++) {
                double angle = startAngle + fullCircle * mobIndex / total;
                Vector2 position = findSpawnPosition(map, hugoX, hugoY, angle, distance);

                Mob mob = createMob(level, position.x, position.y);
                if (level == 4 || level == 5) {
                    mob.setProjectileSprite(getProjectileSprite());
                }
                mob.getAttackDamage().addMultiplier(waveManager.getDamageMultiplier());
                mob.setXpReward(MobXp.rewardFor(level, waveManager.getCycle()));
                mobs.add(mob);
                mobIndex++;
            }
        }

        System.out.println("Wave " + waveManager.getWave() + ": " + mobIndex + " mobs spawned");
    }

    // find a spawn position
    private Vector2 findSpawnPosition(GameMap map, float x, float y, double angle, float distance) {
        Vector2 bestPosition = new Vector2(x, y);
        float bestScore = -1f;

        for (int attempt = 0; attempt < 48; attempt++) {
            double direction = angle + attempt * Math.PI / 12;
            float radius = distance + (attempt / 24) * 120f;
            float dx = (float) Math.cos(direction) * radius;
            float dy = (float) Math.sin(direction) * radius;
            Vector2 position = map.move(x, y, dx, dy);
            float distanceSquared = position.dst2(x, y);
            boolean hasRoom = hasRoom(null, position.x, position.y);
            if (hasRoom && distanceSquared >= 320f * 320f) {
                return position;
            }

            float score = hasRoom ? distanceSquared : distanceSquared / 4f;
            if (score > bestScore) {
                bestPosition = position;
                bestScore = score;
            }
        }
        return bestPosition;
    }

    // select the mob type
    private Mob createMob(int level, float x, float y) {
        return switch (level) {
            case 2 ->
                    new MobLvl2(
                            texture("mobs/german-shepherd-walk.png"),
                            texture("mobs/german-shepherd-bite.png"),
                            x,
                            y);
            case 3 ->
                    new MobLvl3(
                            texture("mobs/machete-walk.png"),
                            texture("mobs/machete-attack.png"),
                            x,
                            y);
            case 4 ->
                    new MobLvl4(
                            texture("mobs/veteran-walk.png"),
                            texture("mobs/veteran-fire.png"),
                            x,
                            y);
            case 5 ->
                    new MobLvl5(
                            texture("mobs/boss-walk.png"),
                            texture("mobs/boss-fire.png"),
                            texture("mobs/boss-knife.png"),
                            x,
                            y);
            default -> new MobLvl1(texture("mobs/soldier-level-1.png"), x, y);
        };
    }

    // parachute kill
    public void defeat(Mob mob) {
        mob.takeDamage(mob.getLife().getCurrentLife());
        hasDefeatedMobs = true;
    }

    public void updateWave(
            WaveManager waveManager, float delta, GameMap map, float hugoX, float hugoY) {
        if (hasDefeatedMobs) {
            mobs.removeIf(mob -> mob.getLife().isDead());
            hasDefeatedMobs = false;
        }

        if (waveManager.update(delta)) {
            spawnWave(waveManager, map, hugoX, hugoY);
        }
    }

    // copper bullet
    private TextureRegion getProjectileSprite() {
        if (projectileSprite == null) {
            Texture texture = texture("projectiles/balle-hugo-cuivre.png");
            projectileSprite = new TextureRegion(texture);
            projectileSprite.flip(false, true);
        }
        return projectileSprite;
    }

    private Texture texture(String path) {
        Texture texture = textures.get(path);
        if (texture == null) {
            texture = loadTexture(path);
            textures.put(path, texture);
        }
        return texture;
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        projectileSprite = null;
        mobs.clear();
    }
}
