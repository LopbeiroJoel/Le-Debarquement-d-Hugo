package hugo.mob.spawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import hugo.map.CarteJeu;
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

    private static final int[][] WAVE_COUNTS = {
        {5, 0,  0,  0, 0},
        {5, 8,  0,  0, 0},
        {5, 5, 10,  0, 0},
        {5, 5,  5, 10, 0},
        {4, 5,  8, 12, 1}
    };

    private final List<Mob> mobs = new ArrayList<>();
    private final Map<String, Texture> textures = new HashMap<>();
    private TextureRegion projectileSprite;
    private boolean hasDefeatedMobs;

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

            float nextDistance = dx * dx + dy * dy;
            if (movingMob != null) {
                float currentDx = movingMob.getX() - other.getX();
                float currentDy = movingMob.getY() - other.getY();
                if (nextDistance > currentDx * currentDx + currentDy * currentDy) {
                    continue;
                }
            }
            if (nextDistance < MIN_MOB_DISTANCE * MIN_MOB_DISTANCE) {
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
        float distance = Math.max(SPAWN_DISTANCE, total * 9f);

        for (int typeIndex = 0; typeIndex < counts.length; typeIndex++) {
            int level = typeIndex + 1;

            for (int i = 0; i < counts[typeIndex]; i++) {
                double angle = startAngle + 2 * Math.PI * mobIndex / total;
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

    private Vector2 findSpawnPosition(CarteJeu map, float x, float y, double angle, float distance) {
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

    public void defeat(Mob mob) {
        mob.takeDamage(mob.getLife().getCurrentLife());
        hasDefeatedMobs = true;
    }

    public void updateWave(WaveManager waveManager, float delta, CarteJeu map, float hugoX, float hugoY) {
        if (hasDefeatedMobs) {
            mobs.removeIf(mob -> mob.getLife().isDead());
            hasDefeatedMobs = false;
        }

        if (waveManager.update(delta)) {
            spawnWave(waveManager, map, hugoX, hugoY);
        }
    }

    private TextureRegion getProjectileSprite() {
        if (projectileSprite == null) {
            Texture texture = textures.computeIfAbsent("projectiles/balle-hugo-cuivre.png", this::loadTexture);
            projectileSprite = new TextureRegion(texture);
            projectileSprite.flip(false, true);
        }
        return projectileSprite;
    }

    private Texture texture(String name) {
        return textures.computeIfAbsent("mobs/" + name, this::loadTexture);
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
