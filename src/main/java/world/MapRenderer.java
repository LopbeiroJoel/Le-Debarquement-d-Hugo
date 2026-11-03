package world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;

public final class MapRenderer implements Disposable {
    private final GameMap world;
    private final Sprites sprites=new Sprites();
    private final List<TextureRegion> tiles=new ArrayList<>();
    private final ShapeRenderer shapes=new ShapeRenderer();
    public final Texture overview;
    private final Texture shadow;
    private final Rectangle view=new Rectangle();

    public MapRenderer(GameMap world){
        this.world=world;
        for(int y=0;y<world.height;y+=1024)for(int x=0;x<world.width;x+=1024){
            Texture t=new Texture(Gdx.files.internal("render/ground/"+x+"-"+y+".png"));
            t.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            TextureRegion region=new TextureRegion(t);region.flip(false,true);tiles.add(region);
        }
        for(AssetType type:AssetType.values())sprites.get(type);
        overview=new Texture(Gdx.files.internal("render/overview.png"));
        Pixmap ellipse=new Pixmap(96,24,Pixmap.Format.RGBA8888);ellipse.setColor(1,1,1,1);
        for(int y=0;y<24;y++)for(int x=0;x<96;x++)if(Math.pow((x-48)/48.0,2)+Math.pow((y-12)/12.0,2)<1)ellipse.drawPixel(x,y);
        shadow=new Texture(ellipse);ellipse.dispose();
    }
    public void draw(SpriteBatch batch,OrthographicCamera camera,float actorY,Runnable drawActor){
        float w=camera.viewportWidth*camera.zoom,h=camera.viewportHeight*camera.zoom;
        view.set(camera.position.x-w/2,camera.position.y-h/2,w,h);
        batch.setProjectionMatrix(camera.combined);batch.begin();
        int index=0;
        for(int y=0;y<world.height;y+=1024)for(int x=0;x<world.width;x+=1024){
            TextureRegion tile=tiles.get(index++);
            if(view.overlaps(new Rectangle(x,y,tile.getRegionWidth(),tile.getRegionHeight())))batch.draw(tile,x,y);
        }
        for(MapEntity e:world.getEntities())if(e.ground&&visible(e))drawEntity(batch,e);
        for(MapEntity e:world.getEntities())if(!e.ground&&visible(e)){
            int size=e.getType().width;
            batch.setColor(15/255f,24/255f,19/255f,e.getType().footprint.equals("tree")?48/255f:32/255f);
            batch.draw(shadow,e.x-size/3f,e.y-8,size*3/4f,17);
        }
        batch.setColor(Color.WHITE);boolean drawn=false;
        for(MapEntity e:world.getEntities())if(!e.ground){
            if(!drawn&&actorY<e.y){drawActor.run();drawn=true;}
            if(visible(e))drawEntity(batch,e);
        }
        if(!drawn)drawActor.run();batch.end();
    }
    private boolean visible(MapEntity e){AssetType t=e.getType();return view.overlaps(new Rectangle(e.x-t.width/2f,e.y-t.height,t.width,t.height+20));}
    private void drawEntity(SpriteBatch batch,MapEntity e){AssetType t=e.getType();batch.draw(sprites.get(t),e.x-t.width/2f,e.y-t.height,t.width,t.height);}
    public void drawCollisions(OrthographicCamera camera){
        shapes.setProjectionMatrix(camera.combined);shapes.begin(ShapeRenderer.ShapeType.Line);shapes.setColor(1,.25f,.2f,1);
        for(MapEntity e:world.getEntities())if(e.collision!=null&&visible(e))rect(e.collision);
        for(Rectangle wall:world.trenchWalls)rect(wall);
        for(float[] polygon:world.areas.get("riverShape").polygons)shapes.polygon(polygon);
        shapes.end();
    }
    private void rect(Rectangle r){shapes.rect(r.x,r.y,r.width,r.height);}
    public void dispose(){for(TextureRegion tile:tiles)tile.getTexture().dispose();sprites.dispose();overview.dispose();shadow.dispose();shapes.dispose();}
}
