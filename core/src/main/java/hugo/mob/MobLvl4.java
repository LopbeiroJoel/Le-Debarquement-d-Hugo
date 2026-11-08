package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MobLvl4 extends AnimatedMob {
    private final Animation<TextureRegion> shot;

    public MobLvl4(Texture walk, Texture fire, float x, float y) {
        super(walk, x, y, 80, 80, 105, 22, 0.14f);
        shot = readAnimation(fire, 0.10f);
    }

    public void playShot() {
        playAnimation(shot);
    }
}
