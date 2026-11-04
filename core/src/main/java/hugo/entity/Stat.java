package hugo.entity;

public class Stat {
    private final float baseValue;
    private float multiplier;

    public Stat(float baseValue) {
        this.baseValue = baseValue;
        this.multiplier = 1.0f;       
    }

    public float getValue() {
        return baseValue * multiplier;
    }

    public void addMultiplier(float factor) {
        if (factor <= 0) {
            return;                    
        }
        multiplier *= factor;       
    }

    public void removeMultiplier(float factor) {
        if (factor <= 0) {
            return;
        }
        multiplier /= factor;
    }
}