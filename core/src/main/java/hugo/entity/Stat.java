package hugo.entity;

public class Stat {

    private final float baseValue;
    private float multiplier;
    private float bonus;

    public Stat(float baseValue) {
        this.baseValue = baseValue;
        this.multiplier = 1.0f;
    }

    public void addBonus(float value) {
        bonus += value;                     
    }

    public void removeBonus(float value) {
        bonus -= value;                     
    }
    public float getValue() {
        return (baseValue + bonus) * multiplier;
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
