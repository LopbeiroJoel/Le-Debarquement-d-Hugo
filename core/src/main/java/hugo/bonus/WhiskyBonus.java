package hugo.bonus;

public final class WhiskyBonus {
    private float remainingTime;

    public void activate() {
        remainingTime = 30f;
    }

    public void update(float delta) {
        remainingTime = Math.max(0f, remainingTime - delta);
    }

    public float getRemainingTime() {
        return remainingTime;
    }
}
