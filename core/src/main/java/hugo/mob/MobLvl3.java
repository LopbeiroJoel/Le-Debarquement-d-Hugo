package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MobLvl3 extends AnimatedMob {
    private final Animation<TextureRegion> slash;

    public MobLvl3(Texture walk, Texture attack, float x, float y) {
        super(walk, x, y, 40, 90, 105, 22, 0.13f);
        slash = readAnimation(attack, 0.09f);
    }

    public void playMacheteAttack() {
        playAnimation(slash);
    }

    protected void onAttack() {
        playMacheteAttack();
    }
}
