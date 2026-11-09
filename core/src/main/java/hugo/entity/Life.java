package hugo.entity;

public class Life {

    private float currentLife;
    private float maxLife;

    public Life(float maxLife) {
        this.maxLife = maxLife;
        this.currentLife = maxLife;
    }

    public float getCurrentLife() {
        return currentLife;
    }

    private void setCurrentLife(float currentLife) {
        this.currentLife = Math.max(0, Math.min(currentLife, maxLife));

    }

    public void takeDamage(float amount) {
        if (amount < 0) {
            return;
        }
        setCurrentLife(currentLife - amount);
    }

    public float getMaxLife() {
        return maxLife;
    }

    protected void setMaxLife(float maxLife) {
        this.maxLife = Math.max(0, maxLife);
    }

    public void heal(float amount) {
        if (amount < 0) {
            return;
        }
        setCurrentLife(currentLife + amount);
    }

    public boolean isDead() {
        return currentLife <= 0;
    }

    public void increaseMaxLife(float factor) {
        if (factor <= 1.0f) {
            return;
        }
        float oldMax = maxLife;
        maxLife *= factor;
        heal(maxLife - oldMax);
    }
}
