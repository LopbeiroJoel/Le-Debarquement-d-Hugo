package hugo.epitech.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.EnumMap;

public final class Sprites implements Disposable {
    private final EnumMap<AssetType,TextureRegion> cache=new EnumMap<>(AssetType.class);
    private final EnumMap<AssetType,GroundShadow> shadows=new EnumMap<>(AssetType.class);
    public TextureRegion get(AssetType type){
        TextureRegion ready=cache.get(type);
        if(ready==null){
            String suffix=type.path.contains("-v3.")?"-v3":"";
            String path="render/sprites/"+type.name()+suffix+".png";
            Pixmap pixels;
            if(Gdx.files.internal(path).exists())pixels=new Pixmap(Gdx.files.internal(path));
            else {
                Pixmap source=new Pixmap(Gdx.files.internal(type.path.substring(1)));
                int left=source.getWidth(),top=source.getHeight(),right=-1,bottom=-1;
                for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)if((source.getPixel(x,y)&255)>16){
                    left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);
                }
                if(right<left){source.dispose();throw new IllegalArgumentException("Sprite vide : "+type.path);}
                pixels=new Pixmap(type.width*3,type.height*3,Pixmap.Format.RGBA8888);
                pixels.setFilter(Pixmap.Filter.NearestNeighbour);
                pixels.drawPixmap(source,left,top,right-left+1,bottom-top+1,0,0,pixels.getWidth(),pixels.getHeight());source.dispose();
            }
            shadows.put(type,createShadow(type,pixels));
            Texture texture=new Texture(pixels);pixels.dispose();
            texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            ready=new TextureRegion(texture);ready.flip(false,true);cache.put(type,ready);
        }
        return ready;
    }
    public static final class GroundShadow {
        public final TextureRegion cast,contact;
        public final float width,height;
        GroundShadow(Pixmap castPixels,Pixmap contactPixels){
            width=castPixels.getWidth()/2f;height=castPixels.getHeight()/2f;
            Texture castTexture=new Texture(castPixels),contactTexture=new Texture(contactPixels);
            castTexture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            contactTexture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            cast=new TextureRegion(castTexture);cast.flip(false,true);
            contact=new TextureRegion(contactTexture);contact.flip(false,true);
        }
    }
    private GroundShadow createShadow(AssetType type,Pixmap source){
        int width=type.width*2,height=type.height*2;
        int outWidth=width+(int)Math.ceil(height*.22)+4,outHeight=height+(int)Math.ceil(height*.14)+8;
        Pixmap cast=new Pixmap(outWidth,outHeight,Pixmap.Format.RGBA8888);
        Pixmap contact=new Pixmap(outWidth,outHeight,Pixmap.Format.RGBA8888);
        cast.setBlending(Pixmap.Blending.None);contact.setBlending(Pixmap.Blending.None);
        boolean tree=type.footprint.equals("tree");
        for(int x=0;x<width;x++){
            int sx=x*source.getWidth()/width,bottom=-1;
            for(int y=height-1;y>=0;y--)if((source.getPixel(sx,y*source.getHeight()/height)&255)>128){bottom=y;break;}
            if(bottom<0)continue;
            int ground=tree?height-1:bottom;
            for(int y=0;y<=bottom;y++){
                int alpha=source.getPixel(sx,y*source.getHeight()/height)&255;
                if(alpha<64)continue;
                int depth=ground-y,px=x+Math.round(depth*.22f),py=ground+Math.round(depth*.14f);
                cast.setColor(1,1,1,alpha/255f);cast.fillRectangle(px,py,2,2);
            }
            if(!tree||Math.abs(x-width/2)<width*.13f){
                int base=tree?height-1:bottom;
                for(int offset=-2;offset<=5;offset++){
                    int py=base+offset;if(py<0||py>=outHeight)continue;
                    int alpha=Math.max(0,200-Math.abs(offset)*35);
                    contact.drawPixel(x,py,0xffffff00|alpha);
                }
            }
        }
        GroundShadow result=new GroundShadow(cast,contact);cast.dispose();contact.dispose();return result;
    }
    public GroundShadow shadow(AssetType type){get(type);return shadows.get(type);}
    public void dispose(){
        for(TextureRegion sprite:cache.values())sprite.getTexture().dispose();
        for(GroundShadow shadow:shadows.values()){shadow.cast.getTexture().dispose();shadow.contact.getTexture().dispose();}cache.clear();shadows.clear();
    }
}
