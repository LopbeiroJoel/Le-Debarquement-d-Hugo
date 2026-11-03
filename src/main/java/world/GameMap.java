package world;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


public final class GameMap {
    public final int width,height;
    public static final double SPAWN_X=742,SPAWN_Y=760;
    public static final double WATER_TOP=850,WATER_BOTTOM=930;
    public static final double BRIDGE_LEFT=717,BRIDGE_RIGHT=767;
    private final BufferedImage ground;
    private final Sprites sprites=new Sprites();
    private final List<MapEntity> entities=new ArrayList<>();
    private final Area water;
    private Consumer<String> chestListener=id->{};

    public GameMap()throws IOException{
        ground=Sprites.read("/assets/terrain.png");width=ground.getWidth();height=ground.getHeight();
        water=new Area(new Rectangle2D.Double(0,WATER_TOP,width,WATER_BOTTOM-WATER_TOP));
        water.subtract(new Area(new Rectangle2D.Double(BRIDGE_LEFT,WATER_TOP-1,BRIDGE_RIGHT-BRIDGE_LEFT,WATER_BOTTOM-WATER_TOP+2)));
        sprites.get(AssetType.OPEN_CHEST);
        buildMap();
        Collections.sort(entities,Comparator.comparingDouble(e->e.y));
        if(!canMove(playerBox(SPAWN_X,SPAWN_Y)))throw new IOException("Point de depart bloque.");
    }

    public void add(AssetType type,double x,double y)throws IOException{
        entities.add(new MapEntity(type.name()+"_"+entities.size(),type,x,y,sprites));
    }

    private void buildMap()throws IOException{
        
        for(int i=0;i<4;i++)add(AssetType.PERIMETER_H,1046+i*92,140);
        for(int i=0;i<3;i++)add(AssetType.PERIMETER_V,1000,140+(i+1)*100);
        for(int i=0;i<3;i++)add(AssetType.PERIMETER_V,1368,140+(i+1)*100);
        
        add(AssetType.PERIMETER_H,1046,440);
        add(AssetType.PERIMETER_H,1138,440);
        add(AssetType.PERIMETER_H,1322,440);
        add(AssetType.BARRACK_LARGE,1080,260);
        add(AssetType.BARRACK_SMALL,1274,260);
        add(AssetType.SHELTER,1080,355);
        add(AssetType.SHELTER,1268,355);
        add(AssetType.CHEST,1225,400);
        add(AssetType.AMMO,1150,290);
        add(AssetType.BARREL,1318,388);
        add(AssetType.BARREL,1339,388);

        
        add(AssetType.RUIN,315,270);
        add(AssetType.CABIN,515,260);
        add(AssetType.LOGS,435,340);
        add(AssetType.CHEST,320,365);

        
        add(AssetType.FENCE_LONG,330,600);
        add(AssetType.FENCE_MEDIUM,420,600);
        add(AssetType.FENCE_WIRE_SHORT,530,570);
        add(AssetType.FENCE_WIRE_MEDIUM,610,570);
        add(AssetType.FENCE_WIRE_LONG,895,570);
        add(AssetType.WIRE,1010,570);
        add(AssetType.HEDGEHOG,1090,595);
        add(AssetType.HEDGEHOG,270,635);
        add(AssetType.BAGS3,350,670);
        add(AssetType.BAGS4,485,650);
        add(AssetType.BAGS5,940,650);
        add(AssetType.GUN,410,700);
        add(AssetType.TANK,1150,690);
        add(AssetType.WRECK,270,780);
        add(AssetType.CRATER1,610,690);
        add(AssetType.CRATER2,1010,740);
        add(AssetType.CRATER_WATER,510,780);
        add(AssetType.BRIDGE,742,943);

        
        Random random=new Random(1944);
        AssetType[] trees={AssetType.PINE_D1,AssetType.PINE_D2,AssetType.PINE_D3,AssetType.PINE_D4,
            AssetType.PINE_L1,AssetType.PINE_L2,AssetType.PINE_L3,AssetType.PINE_L4,
            AssetType.LEAF1,AssetType.LEAF2,AssetType.LEAF3,AssetType.LEAF4};
        for(int i=0;i<100;i++){
            double x,y;
            if(i<50){x=65+random.nextInt(width-130);y=90+random.nextInt(65);}
            else if(i<75){x=45+random.nextInt(130);y=220+random.nextInt(540);}
            else{x=width-165+random.nextInt(120);y=490+random.nextInt(300);}
            add(trees[i%trees.length],x,y);
        }
        for(int i=0;i<8;i++)add(trees[i%trees.length],170+i*160,1030);
    }

    public int entityCount(){return entities.size();}
    public List<MapEntity> getEntities(){return Collections.unmodifiableList(entities);}
    public void setChestListener(Consumer<String> listener){chestListener=listener==null?id->{}:listener;}
    public static Rectangle2D.Double playerBox(double x,double feetY){return new Rectangle2D.Double(x-6,feetY-10,12,10);}
    public boolean canMove(Rectangle2D box){
        if(box.getMinX()<0||box.getMinY()<0||box.getMaxX()>width||box.getMaxY()>height)return false;
        if(water.intersects(box))return false;
        for(MapEntity e:entities)if(e.collision!=null&&e.collision.intersects(box))return false;
        return true;
    }
    
    public Point2D.Double move(double x,double y,double dx,double dy){
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/2.0));
        for(int i=0;i<steps;i++){
            if(canMove(playerBox(x+dx/steps,y)))x+=dx/steps;
            if(canMove(playerBox(x,y+dy/steps)))y+=dy/steps;
        }
        return new Point2D.Double(x,y);
    }
    public boolean blocksShot(double x1,double y1,double x2,double y2){
        for(MapEntity e:entities)if(e.collision!=null&&e.collision.intersectsLine(x1,y1,x2,y2))return true;
        return false;
    }
    public void updateTriggers(double playerX,double playerY){
        for(MapEntity e:entities)if(e.isChest()&&!e.opened&&Point2D.distance(playerX,playerY,e.x,e.y)<29){
            e.open();chestListener.accept(e.id);
        }
    }
    public void reset(){for(MapEntity e:entities)e.reset();}
    public void draw(Graphics2D g,double playerY,Runnable drawPlayer,boolean debug){
        g.drawImage(ground,0,0,null);
        
        g.setColor(new Color(150,123,75));g.setStroke(new BasicStroke(42));
        g.drawLine(0,790,width,390);g.drawLine(742,590,742,height);
        g.setColor(new Color(73,77,48));g.fillRect(0,(int)WATER_TOP-6,width,(int)(WATER_BOTTOM-WATER_TOP)+12);
        g.setColor(new Color(38,83,93));g.fillRect(0,(int)WATER_TOP,width,(int)(WATER_BOTTOM-WATER_TOP));
        g.setColor(new Color(60,108,114));
        for(int x=10;x<width;x+=39)g.fillRect(x,(int)WATER_TOP+20+(x%3)*12,17,2);
        for(MapEntity e:entities)if(e.ground)e.draw(g);
        boolean drawn=false;
        for(MapEntity e:entities)if(!e.ground){
            if(!drawn&&playerY<e.y){drawPlayer.run();drawn=true;}
            e.draw(g);
        }
        if(!drawn)drawPlayer.run();
        if(debug){
            g.setStroke(new BasicStroke(.6f));g.setColor(new Color(240,70,55,180));g.draw(water);
            for(MapEntity e:entities)if(e.collision!=null)g.draw(e.collision);
        }
    }
}
