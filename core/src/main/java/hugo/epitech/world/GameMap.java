package hugo.epitech.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class GameMap {
    public static final float SPAWN_X=2200,SPAWN_Y=1500,WATER_TOP=2330,WATER_BOTTOM=2510;
    public static final int[] BRIDGES={1250,3420};
    public final int width,height;
    private final List<MapEntity> entities=new ArrayList<>();
    private final Map<Integer,List<MapEntity>> cells=new HashMap<>();
    public final List<Rectangle> trenchWalls=new ArrayList<>();
    public final Map<String,MapArea> areas=new HashMap<>();
    private Consumer<String> chestListener=id->{};

    public GameMap()throws IOException{
        JsonValue data;
        try(InputStream input=GameMap.class.getResourceAsStream("/maps/world.json")){
            if(input==null)throw new IOException("Map introuvable");
            data=new JsonReader().parse(input);
        }
        width=data.getInt("width");height=data.getInt("height");
        for(JsonValue area=data.get("areas").child;area!=null;area=area.next)areas.put(area.name,new MapArea(area));
        for(JsonValue e=data.get("entities").child;e!=null;e=e.next)add(AssetType.valueOf(e.getString("type")),e.getFloat("x"),e.getFloat("y"));
        trenchWalls.add(new Rectangle(3330,1620,870,18));trenchWalls.add(new Rectangle(3330,1683,830,18));
        trenchWalls.add(new Rectangle(3510,1810,650,18));trenchWalls.add(new Rectangle(3570,1873,590,18));
        trenchWalls.add(new Rectangle(3570,2010,750,18));trenchWalls.add(new Rectangle(3510,2073,810,18));
        if(!canMove(playerBox(SPAWN_X,SPAWN_Y)))throw new IOException("Depart bloque");
    }
    public void add(AssetType type,float x,float y){
        MapEntity e=new MapEntity(type.name()+"_"+entities.size(),type,x,y);entities.add(e);
        entities.sort(Comparator.comparingDouble(item->item.y));
        if(e.collision!=null){
            Rectangle box=e.collision;
            for(int cy=(int)box.y/128;cy<=(int)(box.y+box.height)/128;cy++)for(int cx=(int)box.x/128;cx<=(int)(box.x+box.width)/128;cx++)
                cells.computeIfAbsent(cy*64+cx,key->new ArrayList<>()).add(e);
        }
    }
    public int entityCount(){return entities.size();}
    public List<MapEntity> getEntities(){return Collections.unmodifiableList(entities);}
    public boolean overlapsRoad(MapEntity e){return areas.get("roads").intersects(new Rectangle(e.x-e.getType().width/2f,e.y-e.getType().height,e.getType().width,e.getType().height));}
    public static Rectangle playerBox(float x,float feetY){return new Rectangle(x-11,feetY-17,22,17);}
    public boolean canMove(Rectangle box){
        if(box.x<0||box.y<0||box.x+box.width>width||box.y+box.height>height)return false;
        boolean bridge=false;
        for(int x:BRIDGES)if(box.x>=x-60&&box.x+box.width<=x+60)bridge=true;
        if(!bridge&&areas.get("riverShape").intersects(box))return false;
        for(Rectangle wall:trenchWalls)if(wall.overlaps(box))return false;
        for(int cy=(int)box.y/128;cy<=(int)(box.y+box.height)/128;cy++)for(int cx=(int)box.x/128;cx<=(int)(box.x+box.width)/128;cx++){
            List<MapEntity> local=cells.get(cy*64+cx);
            if(local!=null)for(MapEntity e:local)if(e.collision.overlaps(box))return false;
        }
        return true;
    }
    public Vector2 move(float x,float y,float dx,float dy){
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/4));
        for(int i=0;i<steps;i++){
            if(canMove(playerBox(x+dx/steps,y)))x+=dx/steps;
            if(canMove(playerBox(x,y+dy/steps)))y+=dy/steps;
        }
        return new Vector2(x,y);
    }
    public boolean blocksShot(float x1,float y1,float x2,float y2){
        for(Rectangle wall:trenchWalls)if(MapArea.segment(wall,x1,y1,x2,y2))return true;
        for(MapEntity e:entities)if(e.collision!=null&&MapArea.segment(e.collision,x1,y1,x2,y2))return true;
        return false;
    }
    public void setChestListener(Consumer<String> listener){chestListener=listener==null?id->{}:listener;}
    public void updateTriggers(float x,float y){
        for(MapEntity e:entities)if(e.isChest()&&!e.opened&&Vector2.dst(x,y,e.x,e.y)<68){e.open();chestListener.accept(e.id);}
    }
    public void reset(){for(MapEntity e:entities)e.reset();}
    public String zoneAt(float x,float y){
        if(y>WATER_TOP-80&&y<WATER_BOTTOM+100)return "LES BERGES";
        if(areas.get("base").contains(x,y))return "BASE MILITAIRE";
        if(areas.get("trenches").contains(x,y))return "LES TRANCHEES";
        if(areas.get("village").contains(x,y))return "VILLAGE EN RUINE";
        if(areas.get("forest").contains(x,y))return "FORET DU NORD";
        if(y>WATER_BOTTOM)return "BATTERIE DU SUD";
        return "LE BOCAGE";
    }
}
