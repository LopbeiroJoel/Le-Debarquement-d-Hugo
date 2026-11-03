package world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;

public final class MapRenderer implements Disposable {
    private final GameMap world;
    private final Sprites sprites=new Sprites();
    private final List<TextureRegion> tiles=new ArrayList<>();
    private final ShapeRenderer shapes=new ShapeRenderer();
    public final TextureRegion overview;
    private final FrameBuffer overviewTarget;
    private final Rectangle view=new Rectangle();

    public MapRenderer(GameMap world){
        this.world=world;
        for(int y=0;y<world.height;y+=1024)for(int x=0;x<world.width;x+=1024){
            Texture t=new Texture(Gdx.files.internal("render/ground/"+x+"-"+y+".png"));
            t.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            TextureRegion region=new TextureRegion(t);region.flip(false,true);tiles.add(region);
        }
        for(AssetType type:AssetType.values())sprites.get(type);
        overviewTarget=new FrameBuffer(Pixmap.Format.RGBA8888,460,320,false);
        OrthographicCamera previewCamera=new OrthographicCamera();previewCamera.setToOrtho(true,world.width,world.height);
        SpriteBatch previewBatch=new SpriteBatch();overviewTarget.begin();ScreenUtils.clear(0,0,0,1);
        draw(previewBatch,previewCamera,0,()->{});overviewTarget.end();previewBatch.dispose();
        overview=new TextureRegion(overviewTarget.getColorBufferTexture());overview.flip(false,true);

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
        for(MapEntity e:world.getEntities())if(!e.ground&&visible(e))drawShadow(batch,e);
        batch.setColor(Color.WHITE);boolean drawn=false;
        for(MapEntity e:world.getEntities())if(!e.ground){
            if(!drawn&&actorY<e.y){drawActor.run();drawn=true;}
            if(visible(e))drawEntity(batch,e);
        }
        if(!drawn)drawActor.run();batch.end();
    }
    private void drawShadow(SpriteBatch batch,MapEntity e){
        AssetType type=e.getType();Sprites.GroundShadow shadow=sprites.shadow(type);
        float x=e.x-type.width/2f,y=e.y-type.height;
        batch.setColor(.10f,.13f,.09f,type.footprint.equals("tree")?.20f:.25f);
        batch.draw(shadow.cast,x,y,shadow.width,shadow.height);
        batch.setColor(.07f,.09f,.055f,.38f);
        batch.draw(shadow.contact,x,y,shadow.width,shadow.height);
        batch.setColor(Color.WHITE);
    }
    private boolean visible(MapEntity e){AssetType t=e.getType();return view.overlaps(new Rectangle(e.x-t.width/2f,e.y-t.height,t.width+t.height*.25f,t.height+t.height*.35f));}
    private void drawEntity(SpriteBatch batch,MapEntity e){AssetType t=e.getType();batch.draw(sprites.get(t),e.x-t.width/2f,e.y-t.height,t.width,t.height);}
    public void drawCollisions(OrthographicCamera camera){
        shapes.setProjectionMatrix(camera.combined);shapes.begin(ShapeRenderer.ShapeType.Line);shapes.setColor(1,.25f,.2f,1);
        for(MapEntity e:world.getEntities())if(e.collision!=null&&visible(e))rect(e.collision);
        for(Rectangle wall:world.trenchWalls)rect(wall);
        for(float[] polygon:world.areas.get("riverShape").polygons)shapes.polygon(polygon);
        shapes.end();
    }
    private void rect(Rectangle r){shapes.rect(r.x,r.y,r.width,r.height);}
    public void dispose(){for(TextureRegion tile:tiles)tile.getTexture().dispose();sprites.dispose();overviewTarget.dispose();shapes.dispose();}
}
