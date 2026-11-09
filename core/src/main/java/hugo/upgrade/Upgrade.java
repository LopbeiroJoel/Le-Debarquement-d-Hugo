package hugo.upgrade;

import hugo.entity.Player;

public abstract class Upgrade {

    protected final String name;
    protected final String imageName;



    public Upgrade(String name, String imageName) {
        this.name = name;
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public abstract void apply(Player player); 
}