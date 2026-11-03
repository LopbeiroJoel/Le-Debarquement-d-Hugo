package world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.EnumMap;

public final class Sprites implements Disposable {
    private final EnumMap<AssetType,TextureRegion> cache=new EnumMap<>(AssetType.class);
    public TextureRegion get(AssetType type){
        TextureRegion ready=cache.get(type);
        if(ready==null){
            String path="render/sprites/"+type.name()+".png";
            Texture texture;
            if(Gdx.files.internal(path).exists())texture=new Texture(Gdx.files.internal(path));
            else {
                Pixmap source=new Pixmap(Gdx.files.internal(type.path.substring(1)));
                int left=source.getWidth(),top=source.getHeight(),right=-1,bottom=-1;
                for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)if((source.getPixel(x,y)&255)>16){
                    left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);
                }
                if(right<left){source.dispose();throw new IllegalArgumentException("Sprite vide : "+type.path);}
                Pixmap cropped=new Pixmap(type.width*3,type.height*3,Pixmap.Format.RGBA8888);
                cropped.setFilter(Pixmap.Filter.NearestNeighbour);
                cropped.drawPixmap(source,left,top,right-left+1,bottom-top+1,0,0,cropped.getWidth(),cropped.getHeight());
                texture=new Texture(cropped);cropped.dispose();source.dispose();
            }
            texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            ready=new TextureRegion(texture);ready.flip(false,true);cache.put(type,ready);
        }
        return ready;
    }
    public void dispose(){for(TextureRegion sprite:cache.values())sprite.getTexture().dispose();cache.clear();}
}
