package hugo.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

final class WeaponSprites implements Disposable {
    private final Map<String, SpriteSheet> sheets = new HashMap<>();
    private final float frameDuration;

    WeaponSprites(float frameDuration) {
        this.frameDuration = frameDuration;
    }

    // hugo holds his current weapon
    boolean draw(
            SpriteBatch batch,
            String weaponName,
            float time,
            float x,
            float y,
            float height,
            boolean facingLeft) {
        SpriteSheet sheet = sheets.get(weaponName);
        if (sheet == null) {
            sheet = load(weaponName);
            if (sheet == null) {
                return false;
            }
            sheets.put(weaponName, sheet);
        }

        int frameIndex = (int) (time / frameDuration) % 4;
        TextureRegion image = sheet.animation.getKeyFrame(time, true);
        int headX = sheet.leftHeadX;
        if (frameIndex % 2 == 1) {
            headX = sheet.rightHeadX;
        }

        // keep the head and feet in place
        float scale = height * 0.85f / sheet.bodyHeight;
        float width = image.getRegionWidth() * scale;
        float headOffset = headX * scale + height * 0.023f;
        float top = y - height * 0.088f - sheet.feetY * scale;

        if (facingLeft) {
            batch.draw(image, x + headOffset, top, -width, image.getRegionHeight() * scale);
        } else {
            batch.draw(image, x - headOffset, top, width, image.getRegionHeight() * scale);
        }
        return true;
    }

    // load each weapon once
    private SpriteSheet load(String weaponName) {
        SpriteSheet sheet = new SpriteSheet();
        switch (weaponName) {
            case "carbine":
                sheet.setLayout(532, 564, 305, 265);
                break;
            case "submachine-gun":
                sheet.setLayout(568, 592, 296, 268);
                break;
            case "shotgun":
                sheet.setLayout(534, 566, 308, 275);
                break;
            case "machine-gun":
                sheet.setLayout(596, 614, 262, 247);
                break;
            default:
                return null;
        }

        sheet.texture = new Texture("weapons/player/" + weaponName + ".png");
        sheet.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] cells =
                TextureRegion.split(
                        sheet.texture, sheet.texture.getWidth() / 2, sheet.texture.getHeight() / 2);

        TextureRegion[] frames = new TextureRegion[4];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = cells[i / 2][i % 2];
            frames[i].flip(false, true);
        }
        sheet.animation = new Animation<>(frameDuration, frames);
        return sheet;
    }

    @Override
    public void dispose() {
        for (SpriteSheet sheet : sheets.values()) {
            sheet.texture.dispose();
        }
        sheets.clear();
    }

    private static class SpriteSheet {
        private Texture texture;
        private Animation<TextureRegion> animation;
        private int bodyHeight;
        private int feetY;
        private int leftHeadX;
        private int rightHeadX;

        private void setLayout(int bodyHeight, int feetY, int leftHeadX, int rightHeadX) {
            this.bodyHeight = bodyHeight;
            this.feetY = feetY;
            this.leftHeadX = leftHeadX;
            this.rightHeadX = rightHeadX;
        }
    }
}
