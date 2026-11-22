// game screen, input and camera
package hugo.map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import hugo.admin.Admin;
import hugo.chest.ChestBonusMenu;
import hugo.entity.Player;
import hugo.entity.Projectile;
import hugo.gameover.GameOverMenu;
import hugo.mob.Mob;
import hugo.mob.spawn.MobSpawner;
import hugo.upgrade.UpgradeMenu;
import hugo.wave.WaveManager;
import hugo.xp.XpBar;

import java.util.ArrayList;
import java.util.List;

public final class MapScreen extends ScreenAdapter {

    private static final double INITIAL_ZOOM = 1.25; // initial camera zoom

    private final UpgradeMenu upgradeMenu = new UpgradeMenu();

    private final boolean smokeTest;

    private GameMap map;
    private MapRenderer renderer;

    private SpriteBatch batch;
    private BitmapFont font;
    private Texture whitePixel;
    private Texture playerTexture;
    private Player hugo;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Projectile> enemyProjectiles = new ArrayList<>();
    private TextureRegion bulletSprite;
    private Texture playerBulletTexture;
    private TextureRegion playerBulletSprite;

    private final XpBar xpBar = new XpBar();
    private final ChestBonusMenu chestBonusMenu = new ChestBonusMenu();
    private final Admin admin = new Admin();
    private final WaveManager waveManager = new WaveManager();
    private final MobSpawner mobSpawner = new MobSpawner();

    private final MapCamera camera = new MapCamera();

    private final OrthographicCamera worldCamera = new OrthographicCamera();

    private final OrthographicCamera uiCamera = new OrthographicCamera();

    private double positionX = GameMap.SPAWN_X;
    private double positionY = GameMap.SPAWN_Y;

    private boolean showCollisions = false;
    private boolean showMinimap = true;

    private int frameCount = 0;

    private InputAdapter mouseInput;

    private boolean playerDead = false;

    private final GameOverMenu gameOverMenu = new GameOverMenu();

    public MapScreen(boolean smokeTest) {
        this.smokeTest = smokeTest;
    }

    public void show() {
        if (map != null) {
            Gdx.input.setInputProcessor(mouseInput);
            return;
        }

        loadMap();
        prepareGraphics();
        mobSpawner.spawnWave(waveManager, map, hugo.getX(), hugo.getY());
        prepareMouse();
    }

    private void loadMap() {
        try {
            map = new GameMap();
            map.setChestListener(id -> chestBonusMenu.open());
        } catch (Exception error) {
            throw new IllegalStateException("Could not load map", error);
        }
    }

    private void prepareGraphics() {
        batch = new SpriteBatch();
        renderer = new MapRenderer(map);
        font = new BitmapFont();
        playerTexture = new Texture("hero-v2.png");
        hugo = new Player(playerTexture, GameMap.SPAWN_X, GameMap.SPAWN_Y);
        hugo.setMap(map);

        Pixmap image = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        image.setColor(Color.WHITE);
        image.fill();

        whitePixel = new Texture(image);
        bulletSprite = new TextureRegion(whitePixel);
        playerBulletTexture = new Texture("projectiles/balle-hugo-cuivre.png");
        playerBulletSprite = new TextureRegion(playerBulletTexture);
        playerBulletSprite.flip(false, true);

        image.dispose();
    }

    private void prepareMouse() {
        mouseInput =
                new InputAdapter() { // mouse wheel zoom
                    public boolean scrolled(float horizontal, float vertical) {
                        camera.zoom = camera.zoom - vertical * 0.1;

                        return true;
                    }
                };

        Gdx.input.setInputProcessor(mouseInput);
    }

    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        if (smokeTest) {
            positionSmokeCamera();
        }
        double elapsedTime = Math.min(delta, 0.05);

        if (!playerDead
                && !chestBonusMenu.isOpen()
                && !upgradeMenu.isOpen()
                && hugo.getPendingUpgrades() > 0) {
            upgradeMenu.open(hugo);
        }

        if (playerDead) {
            if (gameOverMenu.update(width, height)) {
                restartGame();
                return;
            }
        } else if (chestBonusMenu.isOpen()) {
            chestBonusMenu.update(hugo, width, height);

        } else if (upgradeMenu.isOpen()) {
            upgradeMenu.update(hugo, width, height);
        } else {
            updatePlayer(elapsedTime);
            map.updateChests(delta);
            map.updateTriggers(hugo.getX(), hugo.getY());
            for (Mob mob : mobSpawner.getMobs()) {
                mob.updateAgainst(hugo, map, (float) elapsedTime, mobSpawner);
                mob.attack(hugo);
                mob.attackTarget(hugo, bulletSprite, enemyProjectiles);
            }
            if (hugo.getLife().isDead() && !playerDead) {
                playerDead = true;
                gameOverMenu.open(hugo.getLevel(), waveManager.getWave(), hugo.getKills());
            }
            for (Projectile projectile : enemyProjectiles) {
                projectile.update((float) elapsedTime);
                if (projectile.isActive() && projectile.getHitbox().overlaps(hugo.getHitbox())) {
                    hugo.takeDamage(projectile.getDamage());
                    projectile.destroy();
                }
            }
            enemyProjectiles.removeIf(projectile -> !projectile.isActive());
            mobSpawner.updateWave(waveManager, delta, map, hugo.getX(), hugo.getY());
        }

        readShortcuts();
        updateCamera(width, height);

        drawWorld();
        drawInterface(width, height);

        if (smokeTest) {
            checkSmokeFrame();
        }
    }

    private void restartGame() {
        Game game = (Game) Gdx.app.getApplicationListener();
        game.setScreen(new MapScreen(false));
        dispose();
    }

    private void updatePlayer(double elapsedTime) { // player input
        double directionX = 0;
        double directionY = 0;

        if (isAnyKeyPressed(Input.Keys.D, Input.Keys.RIGHT)) {
            directionX = directionX + 1;
        }

        if (isAnyKeyPressed(Input.Keys.Q, Input.Keys.A, Input.Keys.LEFT)) {
            directionX = directionX - 1;
        }

        if (isAnyKeyPressed(Input.Keys.S, Input.Keys.DOWN)) {
            directionY = directionY + 1;
        }
        if (isAnyKeyPressed(Input.Keys.Z, Input.Keys.W, Input.Keys.UP)) {
            directionY = directionY - 1;
        }

        double length = Math.hypot(directionX, directionY);

        if (length > 0) {
            directionX = directionX / length;
            directionY = directionY / length;
        }

        hugo.setDirection((float) directionX, (float) directionY);
        hugo.update((float) elapsedTime);

        if (hugo.canShoot()) {
            Mob target = findClosestMob();
            if (target != null) {
                for (Projectile projectile : hugo.shoot(playerBulletSprite, target)) {
                    projectile.setMap(map);
                    projectiles.add(projectile);
                }
            }
        }

        for (Projectile projectile : projectiles) {
            projectile.update((float) elapsedTime);
        }
        checkProjectileHits();
        projectiles.removeIf(projectile -> !projectile.isActive());
        mobSpawner.getMobs().removeIf(mob -> mob.getLife().isDead());

        positionX = hugo.getX();
        positionY = hugo.getCenterY();
    }

    private void checkProjectileHits() {
        for (Projectile projectile : projectiles) {
            for (Mob mob : mobSpawner.getMobs()) {
                if (!projectile.isActive() || mob.getLife().isDead()) {
                    continue;
                }
                if (projectile.getHitbox().overlaps(mob.getHitbox())) {
                    mob.getLife().takeDamage(projectile.getDamage());
                    projectile.destroy();
                    if (mob.getLife().isDead()) {
                        hugo.registerKill(mob.getXpReward());
                    }
                }
            }
        }
    }

    private Mob findClosestMob() {
        Mob closest = null;
        float closestDistance = Float.MAX_VALUE;
        float range = hugo.getShootRange();

        for (Mob mob : mobSpawner.getMobs()) {
            if (mob.getLife().isDead()) {
                continue;
            }
            float dx = mob.getX() - hugo.getX();
            float dy = mob.getCenterY() - hugo.getCenterY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= range && distance < closestDistance) {
                closestDistance = distance;
                closest = mob;
            }
        }
        return closest;
    }

    private boolean isAnyKeyPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }

        return false;
    }

    private void readShortcuts() { // keyboard shortcuts
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            hugo.toggleSuperSaiyan(whitePixel);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            admin.killAllMobs(mobSpawner);
            enemyProjectiles.clear();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showCollisions = !showCollisions;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            showMinimap = !showMinimap;
        }

        boolean zoomIn =
                Gdx.input.isKeyJustPressed(Input.Keys.PLUS)
                        || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)
                        || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ADD);

        if (zoomIn) {
            camera.zoom = camera.zoom + 0.15;
        }

        boolean zoomOut =
                Gdx.input.isKeyJustPressed(Input.Keys.MINUS)
                        || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_SUBTRACT);

        if (zoomOut) {
            camera.zoom = camera.zoom - 0.15;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetMapView();
        }
    }

    private void resetMapView() {
        positionX = GameMap.SPAWN_X;
        positionY = GameMap.SPAWN_Y;

        camera.zoom = INITIAL_ZOOM;

        map.reset();
        chestBonusMenu.close();
    }

    private void updateCamera(int width, int height) {
        camera.follow(positionX, positionY, width, height, map.width, map.height);

        positionX = camera.x;
        positionY = camera.y;

        worldCamera.setToOrtho(true, width, height);

        worldCamera.zoom = (float) (1 / camera.zoom);

        worldCamera.position.set((float) camera.x, (float) camera.y, 0);

        worldCamera.update();
    }

    private void drawWorld() {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1);

        renderer.draw(batch, worldCamera, hugo, mobSpawner.getMobs());

        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }
        for (Projectile projectile : enemyProjectiles) {
            projectile.draw(batch);
        }
        for (Mob mob : mobSpawner.getMobs()) {
            mob.drawHealthBar(batch, whitePixel);
        }
        hugo.drawHealthBar(batch, whitePixel);
        batch.end();

        if (showCollisions) {
            renderer.drawCollisions(worldCamera);
        }
    }

    private void drawInterface(int width, int height) {
        uiCamera.setToOrtho(false, width, height);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        if (showMinimap) {
            drawMinimap(width, height);
        }

        drawHelp();

        if (showCollisions) {
            drawDebugInfo(height);
        }

        batch.setColor(Color.WHITE);
        xpBar.draw(batch, whitePixel, font, hugo, height);
        upgradeMenu.draw(batch, whitePixel, width, height);
        chestBonusMenu.draw(batch, whitePixel, font, width, height);
        gameOverMenu.draw(batch, whitePixel, width, height);
        batch.end();
    }

    private void drawMinimap(int width, int height) { // minimap
        float x = width - 254;
        float y = height - 184;

        drawRectangle(x - 7, y - 7, 244, 174, 0.04f, 0.07f, 0.05f, 0.85f);

        batch.setColor(Color.WHITE);

        batch.draw(renderer.overview, x, y, 230, 160);

        drawMinimapView(x, y, width, height);
    }

    private void drawMinimapView(
            float minimapX, float minimapY, int screenWidth, int screenHeight) {
        double viewWidth = screenWidth / camera.zoom;
        double viewHeight = screenHeight / camera.zoom;

        double viewLeft = camera.x - viewWidth / 2; // minimap view box
        double viewBottom = camera.y + viewHeight / 2;

        float left =
                minimapX // camera position on the minimap
                        + (float) (viewLeft / map.width * 230);

        float bottom = minimapY + 160 - (float) (viewBottom / map.height * 160);

        float frameWidth = (float) (viewWidth / map.width * 230);

        float frameHeight = (float) (viewHeight / map.height * 160);

        batch.setColor(1, 1, 0.85f, 0.8f); // view box color

        batch.draw(whitePixel, left, bottom, frameWidth, 1);

        batch.draw(whitePixel, left, bottom + frameHeight, frameWidth, 1);

        batch.draw(whitePixel, left, bottom, 1, frameHeight);

        batch.draw(whitePixel, left + frameWidth, bottom, 1, frameHeight);

        batch.setColor(Color.WHITE);
    }

    private void drawHelp() {
        drawRectangle(20, 17, 478, 27, 0.04f, 0.07f, 0.05f, 0.72f);

        font.setColor(0.9f, 0.9f, 0.8f, 1);

        font.draw(batch, "ZQSD / WASD / Arrows: move    Wheel: zoom    M: map", 30, 35);
    }

    private void drawDebugInfo(int height) {
        int x = (int) camera.x;
        int y = (int) camera.y;
        int framesPerSecond = Gdx.graphics.getFramesPerSecond();

        String info =
                map.entityCount() + " objects | " + x + ", " + y + " | " + framesPerSecond + " FPS";

        font.draw(batch, info, 28, height - 70);
    }

    private void drawRectangle(
            float x,
            float y,
            float width,
            float height,
            float red,
            float green,
            float blue,
            float opacity) {
        batch.setColor(red, green, blue, opacity);

        batch.draw(whitePixel, x, y, width, height);

        batch.setColor(Color.WHITE);
    }

    private void positionSmokeCamera() {
        if (frameCount == 30) {
            positionX = 3100;
            positionY = 850;
        }

        if (frameCount == 60) {
            positionX = 3900;
            positionY = 1850;
        }
    }

    private void checkSmokeFrame() {
        frameCount = frameCount + 1;

        boolean saveCapture = frameCount == 30 || frameCount == 60 || frameCount == 90;

        if (!saveCapture) {
            return;
        }

        saveScreenshot();

        Gdx.app.log(
                "SmokeTest",
                "libGDX : "
                        + map.entityCount()
                        + " objects, OpenGL rendering, "
                        + frameCount
                        + " frames rendered successfully");

        if (frameCount == 90) {
            Gdx.app.exit();
        }
    }

    private void saveScreenshot() {
        String fileName = "libgdx-smoke.png";

        if (frameCount == 60) {
            fileName = "libgdx-base.png";
        }

        if (frameCount == 90) {
            fileName = "libgdx-tranchees.png";
        }

        String defaultDirectory = System.getProperty("java.io.tmpdir") + "/hugo-map-check";

        String directory = System.getProperty("hugo.smokeDir", defaultDirectory);

        Pixmap capture =
                Pixmap.createFromFrameBuffer(
                        0,
                        0,
                        Gdx.graphics.getBackBufferWidth(),
                        Gdx.graphics.getBackBufferHeight());

        try {
            PixmapIO.writePNG(Gdx.files.absolute(directory + "/" + fileName), capture, -1, true);
        } finally {
            capture.dispose();
        }
    }

    public void hide() {
        if (Gdx.input.getInputProcessor() == mouseInput) {
            Gdx.input.setInputProcessor(null);
        }
    }

    public void dispose() {
        hide();

        if (renderer != null) {
            renderer.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }

        if (font != null) {
            font.dispose();
        }

        mobSpawner.dispose();
        upgradeMenu.dispose();
        chestBonusMenu.dispose();
        gameOverMenu.dispose();

        if (whitePixel != null) {
            whitePixel.dispose();
        }
        if (hugo != null) {
            hugo.disposeAnimations();
        }
        if (playerBulletTexture != null) {
            playerBulletTexture.dispose();
        }
        if (playerTexture != null) {
            playerTexture.dispose();
        }
    }
}
