package hugo.epitech.world;

import com.badlogic.gdx.math.Rectangle;

public final class MapEntity {
    public final String id;
    public final float x,y;
    public final boolean ground;
    public final Rectangle collision;
    private AssetType type;
    public boolean opened;

    public MapEntity(String id,AssetType type,float x,float y){
        this.id=id;this.type=type;this.x=x;this.y=y;
        ground=type.footprint.equals("ground");
        float a=.1f,b=.55f,c=.8f,d=.45f;
        if(type.footprint.equals("tree")){a=.43f;b=.87f;c=.14f;d=.13f;}
        if(type.footprint.equals("building")){a=.08f;b=.52f;c=.84f;d=.48f;}
        if(type.footprint.equals("fence")){a=.01f;b=.70f;c=.98f;d=.30f;}
        if(type.footprint.equals("vertical")){a=.20f;b=.02f;c=.60f;d=.98f;}
        collision=ground?null:new Rectangle(x-type.width/2f+a*type.width,y-type.height+b*type.height,c*type.width,d*type.height);
    }
    public AssetType getType(){return type;}
    public boolean isChest(){return type==AssetType.CHEST||type==AssetType.OPEN_CHEST;}
    public void open(){opened=true;type=AssetType.OPEN_CHEST;}
    public void reset(){if(isChest()){opened=false;type=AssetType.CHEST;}}
}
