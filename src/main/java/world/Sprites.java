package world;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import javax.imageio.ImageIO;

public final class Sprites {
    private final EnumMap<AssetType,BufferedImage> cache=new EnumMap<>(AssetType.class);
    public static BufferedImage read(String path)throws IOException{
        try(InputStream in=Sprites.class.getResourceAsStream(path)){
            if(in==null)throw new IOException("Ressource introuvable : "+path);
            BufferedImage image=ImageIO.read(in);
            if(image==null)throw new IOException("Image illisible : "+path);
            return image;
        }
    }
    public static BufferedImage prepare(BufferedImage source,int width,int height)throws IOException{
        int left=source.getWidth(),top=source.getHeight(),right=-1,bottom=-1;
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)if((source.getRGB(x,y)>>>24)>16){
            left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);
        }
        if(right<left)throw new IOException("Sprite vide");
        BufferedImage result=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(source,0,0,width,height,left,top,right+1,bottom+1,null);g.dispose();return result;
    }
    public BufferedImage get(AssetType type)throws IOException{
        BufferedImage ready=cache.get(type);
        if(ready==null){ready=prepare(read(type.path),type.width*3,type.height*3);cache.put(type,ready);}
        return ready;
    }
}
