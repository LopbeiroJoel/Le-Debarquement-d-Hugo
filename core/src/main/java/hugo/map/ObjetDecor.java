//placement asset dans la map clauder
package hugo.map;

import com.badlogic.gdx.math.Rectangle;

public final class ObjetDecor {
    public final String id;
    public final float x,y;
    public final boolean ground;
    public final Rectangle collision;
    private TypeDecor type;
    private static final float CHEST_ANIMATION_DURATION = 0.45f;
    private static final float CHEST_OPEN_DURATION = 120f;
    private float chestTime = -1f;
    private boolean chestArmed = true;

    public ObjetDecor(String id,TypeDecor type,float x,float y){
        this.id=id;this.type=type;this.x=x;this.y=y;
        ground=type.footprint.equals("ground");
        float a=.1f,b=.55f,c=.8f,d=.45f;
        if(type.footprint.equals("tree")){a=.43f;b=.87f;c=.14f;d=.13f;}
        if(type.footprint.equals("building")){a=.08f;b=.52f;c=.84f;d=.48f;}
        if(type.footprint.equals("fence")){a=.01f;b=.70f;c=.98f;d=.30f;}
        if(type.footprint.equals("vertical")){a=.20f;b=.02f;c=.60f;d=.98f;}
        collision=ground?null:new Rectangle(x-type.width/2f+a*type.width,y-type.height+b*type.height,c*type.width,d*type.height);
    }
    public TypeDecor getType(){return type;}
    public boolean isChest(){return type==TypeDecor.CHEST||type==TypeDecor.OPEN_CHEST;}
    public void checkChestTrigger(boolean nearby) {
        if (!nearby) {
            chestArmed = true;
        } else if (chestArmed && chestTime < 0f) {
            chestArmed = false;
            chestTime = 0f;
        }
    }

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
