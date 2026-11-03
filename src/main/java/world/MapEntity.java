package world;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.io.IOException;


public final class MapEntity {
    public final String id;
    public final double x,y;
    public final boolean ground;
    public final Rectangle2D.Double collision;
    private AssetType type;
    private final Sprites sprites;
    public boolean opened;
    public MapEntity(String id,AssetType type,double x,double y,Sprites sprites)throws IOException{
        this.id=id;this.type=type;this.x=x;this.y=y;this.sprites=sprites;
        ground=type.footprint.equals("ground");
        double a=.1,b=.55,c=.8,d=.45;
        if(type.footprint.equals("tree")){a=.43;b=.87;c=.14;d=.13;}
        if(type.footprint.equals("building")){a=.08;b=.52;c=.84;d=.48;}
        if(type.footprint.equals("fence")){a=.01;b=.70;c=.98;d=.30;}
        if(type.footprint.equals("vertical")){a=.20;b=.02;c=.60;d=.98;}
        collision=ground?null:new Rectangle2D.Double(x-type.width/2.0+a*type.width,
            y-type.height+b*type.height,c*type.width,d*type.height);
        sprites.get(type);
    }
    public AssetType getType(){return type;}
    public boolean isChest(){return type==AssetType.CHEST||type==AssetType.OPEN_CHEST;}
    public void open(){opened=true;type=AssetType.OPEN_CHEST;}
    public void reset(){if(isChest()){opened=false;type=AssetType.CHEST;}}
    public void draw(Graphics2D g){
        try{BufferedImage image=sprites.get(type);g.drawImage(image,(int)Math.round(x-type.width/2.0),
            (int)Math.round(y-type.height),null);}catch(IOException e){throw new IllegalStateException(e);}
    }
}
