package hugo.upgrade;

import hugo.entity.Player;

public abstract class Upgrade {

    protected final String name;

    public Upgrade(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void apply(Player player); 
}