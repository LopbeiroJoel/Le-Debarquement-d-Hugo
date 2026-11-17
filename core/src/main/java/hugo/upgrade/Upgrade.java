package hugo.upgrade;

import hugo.entity.Player;

public abstract class Upgrade {

    protected final String name;
    protected final String imageName;
    protected final float weight;

    public Upgrade(String name, String imageName, float weight) {
        this.name = name;
        this.imageName = imageName;
        this.weight = weight;
    }

    public Upgrade(String name, String imageName) {
        this(name, imageName, 1.0f);
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public float getWeight() {
        return weight;
    }

    public boolean isAvailableFor(Player player) {
        return true;
    }

    public abstract void apply(Player player);
}
