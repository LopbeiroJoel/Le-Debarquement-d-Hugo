package hugo.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Entity {

    public void update(float deltaTime);

    public void draw(SpriteBatch batch);
}
