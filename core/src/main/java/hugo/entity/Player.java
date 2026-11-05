package hugo.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player extends Creature {             

    private static final float MAX_LIFE = 100;
    private static final float BASE_SPEED = 400;
    private static final float WIDTH = 96;
    private static final float HEIGHT = 128;
    private static final int FRAME_WIDTH = 543;
    private static final int FRAME_HEIGHT = 724;
    private static final float ATTACK_RANGE = 300;
    private static final float ATTACK_DAMAGE = 7;
    private static final float HITBOX_SIZE = 22;
    private static final float CRIT_CHANCE = 0.1f;
    private static final float CRIT_MULTIPLIER = 2.0f;


    private float xp;
    private Stat critChance;                
    private Stat critMultiplier;



    public Player(Texture sheet, float x, float y) {
        super(new TextureRegion(sheet, 0, 0, FRAME_WIDTH, FRAME_HEIGHT), x, y, MAX_LIFE, BASE_SPEED, WIDTH, HEIGHT, ATTACK_RANGE, ATTACK_DAMAGE, HITBOX_SIZE); 
        this.xp = 0;               
        this.critChance = new Stat(CRIT_CHANCE); 
        this.critMultiplier = new Stat(CRIT_MULTIPLIER);
        }

    public float getXp() {
        return xp;
    }

    public Stat getCritChance() {
        return critChance;
    }

    public Stat getCritMultiplier() {
        return critMultiplier;
    }

    public void addXp(float amount) {
        if (amount < 0) {
            return;
        }
        this.xp += amount;
    }
}
