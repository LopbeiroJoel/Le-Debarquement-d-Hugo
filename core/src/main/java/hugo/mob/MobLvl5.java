package hugo.mob;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import hugo.entity.Creature;
import hugo.entity.Projectile;

import java.util.List;

public class MobLvl5 extends AnimatedMob {
    private final Animation<TextureRegion> shot;
    private final Animation<TextureRegion> knife;

    public MobLvl5(Texture walk, Texture fire, Texture slash, float x, float y) {
        super(walk, x, y, 250, 60, 145, 34, 0.17f);
        shot = readAnimation(fire, 0.11f);
        knife = readAnimation(slash, 0.12f);
    }

    public void attackTarget(Creature target, TextureRegion bullet, List<Projectile> projectiles) {
        if (!startMeleeAttack(target) && shootAt(target, bullet, projectiles, 380f)) {
            playShot();
        }
    }

    protected void onAttack() {
        playKnifeAttack();
    }

    public void playShot() {
        playAnimation(shot);
    }

    public void playKnifeAttack() {
        playAnimation(knife);
    }
}
