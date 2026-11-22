package hugo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;

public final class DecorSprites implements Disposable {
    private final EnumMap<DecorType, TextureRegion> sprites = new EnumMap<>(DecorType.class);
    private final EnumMap<DecorType, GroundShadow> shadows = new EnumMap<>(DecorType.class);

    // load each sprite once
    public TextureRegion get(DecorType type) {
        TextureRegion sprite = sprites.get(type);
        if (sprite != null) {
            return sprite;
        }

        String suffix = type.path.contains("-v3.") ? "-v3" : "";
        FileHandle file = Gdx.files.internal("render/sprites/" + type.name() + suffix + ".png");
        Pixmap pixels = file.exists() ? new Pixmap(file) : loadOriginalSprite(type);
        shadows.put(type, createShadow(type, pixels));

        sprite = createRegion(pixels);
        pixels.dispose();
        sprites.put(type, sprite);
        return sprite;
    }

    // crop the sprite if needed
    private Pixmap loadOriginalSprite(DecorType type) {
        Pixmap source = new Pixmap(Gdx.files.internal(type.path.substring(1)));
        int left = source.getWidth();
        int top = source.getHeight();
        int right = -1;
        int bottom = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                if ((source.getPixel(x, y) & 255) > 16) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        }

        if (right < left) {
            source.dispose();
            throw new IllegalArgumentException("Empty sprite: " + type.path);
        }

        Pixmap pixels = new Pixmap(type.width * 3, type.height * 3, Pixmap.Format.RGBA8888);
        pixels.setFilter(Pixmap.Filter.NearestNeighbour);
        pixels.drawPixmap(
                source,
                left,
                top,
                right - left + 1,
                bottom - top + 1,
                0,
                0,
                pixels.getWidth(),
                pixels.getHeight());
        source.dispose();
        return pixels;
    }

    private static TextureRegion createRegion(Pixmap pixels) {
        Texture texture = new Texture(pixels);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion region = new TextureRegion(texture);
        region.flip(false, true);
        return region;
    }

    public static final class GroundShadow {
        public final TextureRegion cast;
        public final TextureRegion contact;
        public final float width;
        public final float height;

        GroundShadow(Pixmap castPixels, Pixmap contactPixels) {
            width = castPixels.getWidth() / 2f;
            height = castPixels.getHeight() / 2f;
            cast = createRegion(castPixels);
            contact = createRegion(contactPixels);
        }
    }

    // create ground shadows
    private GroundShadow createShadow(DecorType type, Pixmap source) {
        int width = type.width * 2;
        int height = type.height * 2;
        int shadowWidth = width + (int) Math.ceil(height * 0.22) + 4;
        int shadowHeight = height + (int) Math.ceil(height * 0.14) + 8;
        Pixmap cast = new Pixmap(shadowWidth, shadowHeight, Pixmap.Format.RGBA8888);
        Pixmap contact = new Pixmap(shadowWidth, shadowHeight, Pixmap.Format.RGBA8888);
        cast.setBlending(Pixmap.Blending.None);
        contact.setBlending(Pixmap.Blending.None);

        boolean tree = type.footprint.equals("tree");
        for (int x = 0; x < width; x++) {
            int sourceX = x * source.getWidth() / width;
            int bottom = height - 1;
            while (bottom >= 0
                    && (source.getPixel(sourceX, bottom * source.getHeight() / height) & 255)
                            <= 128) {
                bottom--;
            }
            if (bottom < 0) {
                continue;
            }

            int ground = tree ? height - 1 : bottom;
            for (int y = 0; y <= bottom; y++) {
                int alpha = source.getPixel(sourceX, y * source.getHeight() / height) & 255;
                if (alpha >= 64) {
                    int depth = ground - y;
                    int shadowX = x + Math.round(depth * 0.22f);
                    int shadowY = ground + Math.round(depth * 0.14f);
                    cast.setColor(1, 1, 1, alpha / 255f);
                    cast.fillRectangle(shadowX, shadowY, 2, 2);
                }
            }

            if (!tree || Math.abs(x - width / 2) < width * 0.13f) {
                for (int offset = -2; offset <= 5; offset++) {
                    int shadowY = ground + offset;
                    if (shadowY >= 0 && shadowY < shadowHeight) {
                        int alpha = Math.max(0, 200 - Math.abs(offset) * 35);
                        contact.drawPixel(x, shadowY, 0xffffff00 | alpha);
                    }
                }
            }
        }

        GroundShadow shadow = new GroundShadow(cast, contact);
        cast.dispose();
        contact.dispose();
        return shadow;
    }

    public GroundShadow shadow(DecorType type) {
        get(type);
        return shadows.get(type);
    }

    @Override
    public void dispose() {
        for (TextureRegion sprite : sprites.values()) {
            sprite.getTexture().dispose();
        }
        for (GroundShadow shadow : shadows.values()) {
            shadow.cast.getTexture().dispose();
            shadow.contact.getTexture().dispose();
        }
        sprites.clear();
        shadows.clear();
    }
}
