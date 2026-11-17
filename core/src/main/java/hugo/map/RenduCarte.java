// dessine la map
package hugo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

import hugo.entity.Creature;
import hugo.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RenduCarte implements Disposable {

    private final CarteJeu map;
    private final List<Creature> drawOrder = new ArrayList<>();
    private final ImagesDecor images = new ImagesDecor();

    // images du sol
    private final List<TextureRegion> groundTiles = new ArrayList<>();

    // contours collisions
    private final ShapeRenderer outlines = new ShapeRenderer();

    // zone visible
    private final Rectangle visibleArea = new Rectangle();
    private final Texture whitePixel = createWhitePixel();

    private final FrameBuffer minimapBuffer;
    public final TextureRegion overview;

    public RenduCarte(CarteJeu map) {
        this.map = map;

        loadGround();
        loadDecor();

        minimapBuffer = createMinimap();

        overview = new TextureRegion(minimapBuffer.getColorBufferTexture());

        overview.flip(false, true);
    }

    // charge le sol
    private void loadGround() {
        for (int y = 0; y < map.height; y = y + 1024) {
            for (int x = 0; x < map.width; x = x + 1024) {
                String path = "render/ground/" + x + "-" + y + ".png";
                Texture texture = new Texture(Gdx.files.internal(path));

                // pixels nets
                texture.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                );

                TextureRegion tile = new TextureRegion(texture);

                tile.flip(false, true);

                groundTiles.add(tile);
            }
        }
    }

    // charge le décor
    private void loadDecor() {
        for (TypeDecor type : TypeDecor.values()) {
            images.get(type);
        }
    }

    // prépare la mini map
    private FrameBuffer createMinimap() {
        FrameBuffer image = new FrameBuffer(
            Pixmap.Format.RGBA8888,
            460,
            320,
            false
        );

        OrthographicCamera camera = new OrthographicCamera();

        camera.setToOrtho(true, map.width, map.height);

        SpriteBatch batch = new SpriteBatch();

        image.begin();

        ScreenUtils.clear(0, 0, 0, 1);

        draw(batch, camera, null, List.of());

        image.end();

        batch.dispose();

        return image;
    }

    // dessine le monde
    public void draw(
        SpriteBatch batch,
        OrthographicCamera camera,
        Creature player,
        List<? extends Creature> enemies
    ) {
        drawOrder.clear();
        if (player != null) {
            drawOrder.add(player);
        }
        drawOrder.addAll(enemies);
        drawOrder.sort(Comparator.comparingDouble(Creature::getY));

        updateVisibleArea(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // sol
        drawGround(batch);

        // détails au sol
        drawGroundDetails(batch);

        // ombres
        drawShadows(batch);

        // décor et persos
        drawDecorAndCharacters(batch);
        if (player instanceof Player hugo && hugo.getWhiskyTime() > 0f) {
            drawWhiskyTint(batch, hugo.getWhiskyTime());
        }

        batch.end();
    }

    // couleurs whisky
    private void drawWhiskyTint(SpriteBatch batch, float remainingTime) {
        float tint = (MathUtils.sin(remainingTime * 1.8f) + 1f) / 2f;
        float red = MathUtils.lerp(0.58f, 1f, tint);
        float green = MathUtils.lerp(0.24f, 0.76f, tint);
        float blue = MathUtils.lerp(0.88f, 0.16f, tint);
        float opacity = 0.30f * Math.min(1f, remainingTime);

        batch.setColor(red, green, blue, opacity);
        batch.draw(whitePixel, visibleArea.x, visibleArea.y, visibleArea.width, visibleArea.height);
        batch.setColor(Color.WHITE);
    }

    private static Texture createWhitePixel() {
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        Texture texture = new Texture(pixel);
        pixel.dispose();
        return texture;
    }

    // limites caméra
    private void updateVisibleArea(OrthographicCamera camera) {
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;

        float left = camera.position.x - width / 2;
        float top = camera.position.y - height / 2;

        visibleArea.set(left, top, width, height);
    }

    // herbe terre et routes
    private void drawGround(SpriteBatch batch) {
        int tileIndex = 0;

        for (int y = 0; y < map.height; y = y + 1024) {
            for (int x = 0; x < map.width; x = x + 1024) {
                TextureRegion tile = groundTiles.get(tileIndex++);

                Rectangle position = new Rectangle(
                    x,
                    y,
                    tile.getRegionWidth(),
                    tile.getRegionHeight()
                );

                // sol visible seulement
                if (visibleArea.overlaps(position)) {
                    batch.draw(tile, x, y);
                }
            }
        }
    }

    // détails au sol
    private void drawGroundDetails(SpriteBatch batch) {
        for (ObjetDecor object : map.getEntities()) {
            if (object.ground && isVisible(object)) {
                drawObject(batch, object);
            }
        }
    }

    // ombres des objets
    private void drawShadows(SpriteBatch batch) {
        for (ObjetDecor object : map.getEntities()) {
            if (!object.ground && isVisible(object)) {
                drawShadow(batch, object);
            }
        }
    }

    private void drawShadow(
        SpriteBatch batch,
        ObjetDecor object
    ) {
        TypeDecor type = object.getType();

        ImagesDecor.GroundShadow shadow = images.shadow(type);

        float left = object.x - type.width / 2f;
        float top = object.y - type.height;

        float opacity = type.footprint.equals("tree") ? 0.20f : 0.25f;

        // ombre sur le côté
        batch.setColor(0.10f, 0.13f, 0.09f, opacity);

        batch.draw(
            shadow.cast,
            left,
            top,
            shadow.width,
            shadow.height
        );

        // ombre au pied
        batch.setColor(0.07f, 0.09f, 0.055f, 0.38f);

        batch.draw(
            shadow.contact,
            left,
            top,
            shadow.width,
            shadow.height
        );

        batch.setColor(Color.WHITE);
    }

    // persos devant ou derrière
    private void drawDecorAndCharacters(SpriteBatch batch) {
        int nextCreature = 0;
        batch.setColor(Color.WHITE);

        for (ObjetDecor object : map.getEntities()) {
            if (object.ground) {
                continue;
            }
            while (nextCreature < drawOrder.size()
                    && drawOrder.get(nextCreature).getY() < object.y) {
                drawOrder.get(nextCreature).draw(batch);
                nextCreature++;
            }
            if (isVisible(object)) {
                drawObject(batch, object);
            }
        }

        while (nextCreature < drawOrder.size()) {
            drawOrder.get(nextCreature).draw(batch);
            nextCreature++;
        }
    }

    // dessine un objet
    private void drawObject(
        SpriteBatch batch,
        ObjetDecor object
    ) {
        TypeDecor type = object.getType();

        TextureRegion image = images.get(type);

        float left = object.x - type.width / 2f;
        float height = object.getRenderHeight();
        float top = object.y - height;

        batch.draw(image, left, top, type.width, height);
    }

    // objet visible
    private boolean isVisible(ObjetDecor object) {
        TypeDecor type = object.getType();

        float left = object.x - type.width / 2f;
        float top = object.y - type.height;

        // place pour son ombre
        float width = type.width + type.height * 0.25f;
        float height = type.height + type.height * 0.35f;

        Rectangle position = new Rectangle(
            left,
            top,
            width,
            height
        );

        return visibleArea.overlaps(position);
    }

    // collisions en rouge
    public void drawCollisions(OrthographicCamera camera) {
        outlines.setProjectionMatrix(camera.combined);

        outlines.begin(ShapeRenderer.ShapeType.Line);
        outlines.setColor(1, 0.25f, 0.2f, 1);

        // collisions du décor
        for (ObjetDecor object : map.getEntities()) {
            if (object.collision != null && isVisible(object)) {
                drawCollisionBox(object.collision);
            }
        }

        // murs des tranchées
        for (Rectangle wall : map.trenchWalls) {
            drawCollisionBox(wall);
        }

        // bord de la rivière
        ZoneCarte river = map.areas.get("riverShape");

        for (float[] outline : river.polygons) {
            outlines.polygon(outline);
        }

        outlines.end();
    }

    // dessine une collision
    private void drawCollisionBox(Rectangle box) {
        outlines.rect(box.x, box.y, box.width, box.height);
    }

    // libère les images
    public void dispose() {
        for (TextureRegion tile : groundTiles) {
            tile.getTexture().dispose();
        }

        images.dispose();
        minimapBuffer.dispose();
        outlines.dispose();
        whitePixel.dispose();
    }
}
