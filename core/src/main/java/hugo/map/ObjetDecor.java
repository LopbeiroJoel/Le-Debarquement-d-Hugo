// place le décor
package hugo.map;

import com.badlogic.gdx.math.Rectangle;

public final class ObjetDecor {
    public final String id;
    public final float x;
    public final float y;
    public final boolean ground;
    public final Rectangle collision;
    private TypeDecor type;
    private static final float CHEST_ANIMATION_DURATION = 0.45f;
    private static final float CHEST_OPEN_DURATION = 120f;
    private float chestTime = -1f;
    private boolean chestArmed = true;

    public ObjetDecor(String id, TypeDecor type, float x, float y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        ground = type.footprint.equals("ground");
        collision = createCollision();
    }

    // partie solide de l’objet
    private Rectangle createCollision() {
        switch (type.footprint) {
            case "ground": return null;
            case "tree": return collisionBox(0.43f, 0.87f, 0.14f, 0.13f);
            case "building": return collisionBox(0.08f, 0.52f, 0.84f, 0.48f);
            case "fence": return collisionBox(0.01f, 0.70f, 0.98f, 0.30f);
            case "vertical": return collisionBox(0.20f, 0.02f, 0.60f, 0.98f);
            default: return collisionBox(0.1f, 0.55f, 0.8f, 0.45f);
        }
    }

    private Rectangle collisionBox(float left, float top, float width, float height) {
        float boxX = x - type.width / 2f + left * type.width;
        float boxY = y - type.height + top * type.height;
        return new Rectangle(boxX, boxY, width * type.width, height * type.height);
    }

    public TypeDecor getType() {
        return type;
    }

    public boolean isChest() {
        return type == TypeDecor.CHEST || type == TypeDecor.OPEN_CHEST;
    }

    // ouvre le coffre
    public void checkChestTrigger(boolean nearby) {
        if (!nearby) {
            chestArmed = true;
        } else if (chestArmed && chestTime < 0f) {
            chestArmed = false;
            chestTime = 0f;
        }
    }

    // ferme le coffre après deux minutes
    public boolean updateChest(float delta) {
        if (chestTime < 0f) {
            return false;
        }

        float previousTime = chestTime;
        chestTime += delta;
        if (chestTime >= 2 * CHEST_ANIMATION_DURATION + CHEST_OPEN_DURATION) {
            chestTime = -1f;
        }
        type = chestProgress() >= 0.5f ? TypeDecor.OPEN_CHEST : TypeDecor.CHEST;
        return previousTime < CHEST_ANIMATION_DURATION && chestTime >= CHEST_ANIMATION_DURATION;
    }

    private float chestProgress() {
        if (chestTime < 0f) {
            return 0f;
        }
        float opening = chestTime / CHEST_ANIMATION_DURATION;
        float closing = (2 * CHEST_ANIMATION_DURATION + CHEST_OPEN_DURATION - chestTime)
            / CHEST_ANIMATION_DURATION;
        return Math.min(1f, Math.min(opening, closing));
    }

    public float getRenderHeight() {
        if (!isChest()) {
            return type.height;
        }

        float progress = chestProgress();
        return TypeDecor.CHEST.height + progress * (TypeDecor.OPEN_CHEST.height - TypeDecor.CHEST.height);
    }

    public void reset() {
        if (isChest()) {
            chestTime = -1f;
            chestArmed = true;
            type = TypeDecor.CHEST;
        }
    }
}
