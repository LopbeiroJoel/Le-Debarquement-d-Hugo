package hugo.bonus;

public final class HelmetBonus {
    private float remainingTime;

    public void activate() {
        remainingTime = 30f;
    }

    public void update(float delta) {
        remainingTime = Math.max(0f, remainingTime - delta);
    }

    public boolean isActive() {
        return remainingTime > 0f;
    }
}
