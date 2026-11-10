package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import hugo.entity.Creature;
import hugo.entity.Projectile;

import java.util.List;

public class MobLvl4 extends AnimatedMob {
    private final Animation<TextureRegion> shot;

    public MobLvl4(Texture walk, Texture fire, float x, float y) {
        super(walk, x, y, 80, 80, 105, 22, 0.14f);
        shot = readAnimation(fire, 0.10f);
    }

    public void attackTarget(Creature target, TextureRegion bullet, List<Projectile> projectiles) {
        if (shootAt(target, bullet, projectiles, 300f)) {
            playShot();
        }
    }

    public void playShot() {
        playAnimation(shot);
    }
}
