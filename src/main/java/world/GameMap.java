package world;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public final class GameMap {
    public final int width=TerrainPainter.WIDTH,height=TerrainPainter.HEIGHT;
    public static final double SPAWN_X=2200,SPAWN_Y=1500;
    public static final double WATER_TOP=TerrainPainter.RIVER_TOP,WATER_BOTTOM=TerrainPainter.RIVER_BOTTOM;
    private final Sprites sprites=new Sprites();
    private final List<MapEntity> entities=new ArrayList<>();
    private final Map<Integer,List<MapEntity>> cells=new HashMap<>();
    private final List<Rectangle2D> trenchWalls=new ArrayList<>();
    private final TerrainPainter terrain=new TerrainPainter();
    private final Area water=new Area(terrain.riverShape);
    private final BufferedImage ground;
    private final BufferedImage overview;
    private Consumer<String> chestListener=id->{};
    private boolean sorted;

    public GameMap()throws IOException{
        for(int x:TerrainPainter.BRIDGES)water.subtract(new Area(new Rectangle2D.Double(x-60,WATER_TOP-1,120,WATER_BOTTOM-WATER_TOP+2)));
        ground=terrain.paint();
        sprites.get(AssetType.OPEN_CHEST);
        buildMap();
        sort();
        if(!canMove(playerBox(SPAWN_X,SPAWN_Y)))throw new IOException("Point de depart bloque");
        overview=new BufferedImage(230,160,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=overview.createGraphics();g.scale(230.0/width,160.0/height);
        draw(g,-100,()->{},false);g.dispose();
    }

    public void add(AssetType type,double x,double y)throws IOException{
        MapEntity e=new MapEntity(type.name()+"_"+entities.size(),type,x,y,sprites);
        entities.add(e);sorted=false;
        if(e.collision!=null){
            Rectangle2D box=e.collision;
            for(int cy=(int)box.getMinY()/128;cy<=(int)box.getMaxY()/128;cy++)
                for(int cx=(int)box.getMinX()/128;cx<=(int)box.getMaxX()/128;cx++)
                    cells.computeIfAbsent(cy*64+cx,key->new ArrayList<>()).add(e);
        }
    }

    private void sort(){if(!sorted){entities.sort(Comparator.comparingDouble(e->e.y));sorted=true;}}

    private void buildMap()throws IOException{
        buildBase();buildVillage();buildTrenches();buildCheckpoint();buildSouth();
        tree(AssetType.TREE_JOEL,650,850);tree(AssetType.TREE_THOMAS,950,850);
        for(int x:TerrainPainter.BRIDGES){
            add(AssetType.BRIDGE,x,WATER_BOTTOM+12);
            add(AssetType.BAGS4,x-110,WATER_TOP-35);add(AssetType.BAGS4,x+110,WATER_BOTTOM+90);
            add(AssetType.HEDGEHOG,x+132,WATER_TOP-75);
        }
        Random r=new Random(811944);
        AssetType[] trees={AssetType.LEAF1,AssetType.LEAF2,AssetType.LEAF3,AssetType.LEAF4,
            AssetType.PINE_D1,AssetType.PINE_D2,AssetType.PINE_D3,AssetType.PINE_D4,
            AssetType.PINE_L1,AssetType.PINE_L2,AssetType.PINE_L3,AssetType.PINE_L4};
        for(int i=0;i<4000&&countTrees()<370;i++){
            double x=100+r.nextInt(width-200),y=210+r.nextInt(height-260);
            if(terrain.base.contains(x,y)||terrain.village.contains(x,y)||terrain.trenches.contains(x,y)||terrain.battery.contains(x,y))continue;
            if(y>WATER_TOP-90&&y<WATER_BOTTOM+170)continue;
            if(x>1750&&x<2550&&y>1180&&y<1800)continue;
            if(x>450&&x<1120&&y>450&&y<1120)continue;
            boolean woods=terrain.forest.contains(x,y)||x<260||x>width-260||y<280;
            if(!woods&&r.nextDouble()>.18)continue;
            AssetType type=woods?trees[r.nextInt(trees.length)]:trees[r.nextInt(4)];
            if(clearOfRoad(type,x,y)&&canMove(new Rectangle2D.Double(x-28,y-18,56,55)))add(type,x,y);
        }
        for(int i=0;i<1000;i++){
            double x=80+r.nextInt(width-160),y=90+r.nextInt(height-150);
            if(x>450&&x<1120&&y>450&&y<1120)continue;
            if(!clearOfRoad(AssetType.BUSH,x,y)||terrain.base.contains(x,y)||terrain.trenches.contains(x,y)||terrain.village.contains(x,y)||y>WATER_TOP-45&&y<WATER_BOTTOM+65)continue;
            if(canMove(new Rectangle2D.Double(x-28,y-10,56,24)))add(AssetType.BUSH,x,y);
            if(entityCount()>930)break;
        }
        for(int i=0;i<24;i++){
            double x=2800+r.nextInt(1500),y=1690+r.nextInt(530);
            if(!terrain.roads.contains(x,y)&&canMove(new Rectangle2D.Double(x-30,y-15,60,30)))add(i%3==0?AssetType.CRATER_WATER:i%2==0?AssetType.CRATER1:AssetType.CRATER2,x,y);
        }
    }

    private int countTrees(){int n=0;for(MapEntity e:entities)if(e.getType().footprint.equals("tree"))n++;return n;}

    private boolean clearOfRoad(AssetType type,double x,double y){return !terrain.roads.intersects(x-type.width/2.0-12,y-type.height-12,type.width+24,type.height+24);}
    private void tree(AssetType type,double x,double y)throws IOException{if(clearOfRoad(type,x,y))add(type,x,y);}
    public boolean overlapsRoad(MapEntity e){return terrain.roads.intersects(e.x-e.getType().width/2.0,e.y-e.getType().height,e.getType().width,e.getType().height);}

    private void buildBase()throws IOException{
        for(int i=0;i<5;i++)add(AssetType.PERIMETER_H,2742+i*184,510);
        for(int i=0;i<3;i++){add(AssetType.PERIMETER_V,2650,720+i*200);add(AssetType.PERIMETER_V,3570,720+i*200);}
        for(int x:new int[]{2742,2926,3294,3478})add(AssetType.PERIMETER_H,x,1120);
        add(AssetType.BARRACK_LARGE,2820,770);add(AssetType.BARRACK_SMALL,3370,760);
        add(AssetType.SHELTER,2820,990);add(AssetType.SHELTER,3370,985);
        add(AssetType.AMMO,2990,850);add(AssetType.AMMO,3240,880);
        for(int i=0;i<4;i++)add(AssetType.BARREL,3430+i%2*46,990+i/2*55);
        add(AssetType.CHEST,3190,1025);add(AssetType.TANK,3880,760);
        add(AssetType.BAGS5,3000,1150);add(AssetType.BAGS5,3220,1150);
        add(AssetType.GUN,3660,970);add(AssetType.BAGS4,3720,1050);
    }

    private void buildVillage()throws IOException{
        add(AssetType.RUIN,830,1350);add(AssetType.CABIN,1200,1240);
        add(AssetType.RUIN,760,1830);add(AssetType.CABIN,1510,1790);
        add(AssetType.LOGS,1090,1830);add(AssetType.BARREL,1450,1840);
        add(AssetType.CHEST,920,1480);add(AssetType.WRECK,480,1630);
        add(AssetType.CRATER2,1070,1610);add(AssetType.CRATER1,1550,1480);
        for(int i=0;i<3;i++)add(AssetType.FENCE_LONG,590+i*180,1090);
        for(int i=0;i<3;i++)add(AssetType.FENCE_MEDIUM,600+i*145,1920);
        add(AssetType.BAGS3,1420,1360);add(AssetType.AMMO,1400,1410);
        tree(AssetType.LEAF4,530,1310);tree(AssetType.LEAF3,1610,1180);
    }

    private void buildCheckpoint()throws IOException{
        add(AssetType.BAGS5,2070,1440);add(AssetType.BAGS4,2360,1430);
        add(AssetType.AMMO,2010,1410);add(AssetType.BARREL,2410,1410);
        add(AssetType.HEDGEHOG,2010,1620);add(AssetType.HEDGEHOG,2390,1610);
        add(AssetType.CHEST,2280,1650);add(AssetType.LOGS,1970,1740);
        tree(AssetType.LEAF4,1840,1410);tree(AssetType.LEAF3,2490,1670);
        add(AssetType.BAGS3,2140,1730);add(AssetType.GUN,2460,1280);
    }

    private void buildTrenches()throws IOException{
        for(int x:new int[]{3475,3765,4055})add(AssetType.TRENCH,x,1705);
        for(int x:new int[]{3655,3945,4160})add(AssetType.TRENCH,x,1895);
        for(int x:new int[]{3655,3945,4235})add(AssetType.TRENCH,x,2095);
        trenchWalls.add(new Rectangle2D.Double(3330,1620,870,18));
        trenchWalls.add(new Rectangle2D.Double(3330,1683,830,18));
        trenchWalls.add(new Rectangle2D.Double(3510,1810,650,18));
        trenchWalls.add(new Rectangle2D.Double(3570,1873,590,18));
        trenchWalls.add(new Rectangle2D.Double(3570,2010,750,18));
        trenchWalls.add(new Rectangle2D.Double(3510,2073,810,18));
        for(int i=0;i<5;i++){
            add(i%2==0?AssetType.FENCE_WIRE_LONG:AssetType.FENCE_WIRE_MEDIUM,3420+i*184,1610);
            add(AssetType.BAGS5,3600+i*158,2120);
        }
        for(int i=0;i<4;i++)add(AssetType.WIRE,3580+i*220,1550);
        add(AssetType.FENCE_WIRE_SHORT,4340,1930);
        add(AssetType.HEDGEHOG,4400,1740);add(AssetType.HEDGEHOG,3240,1870);
        add(AssetType.WRECK,3990,2230);add(AssetType.GUN,4330,2160);
        add(AssetType.AMMO,4050,1780);add(AssetType.BARREL,4090,1980);
        add(AssetType.CHEST,3760,1940);add(AssetType.SHELTER,4380,1830);
    }

    private void buildSouth()throws IOException{
        add(AssetType.GUN,2440,2810);add(AssetType.GUN,2760,2790);add(AssetType.GUN,3070,2820);
        for(int i=0;i<7;i++)add(i%2==0?AssetType.BAGS5:AssetType.BAGS4,2310+i*135,2720);
        add(AssetType.SHELTER,2780,3130);add(AssetType.AMMO,2600,3080);
        add(AssetType.CHEST,3000,3090);add(AssetType.BARREL,2880,3110);
        add(AssetType.TANK,3850,2780);add(AssetType.WRECK,4020,2980);
        add(AssetType.CABIN,640,2770);add(AssetType.LOGS,850,2820);
        add(AssetType.CHEST,730,2920);add(AssetType.CRATER_WATER,1750,2730);
    }

    public String zoneAt(double x,double y){
        if(y>WATER_TOP-80&&y<WATER_BOTTOM+100)return "LES BERGES";
        if(terrain.base.contains(x,y))return "BASE MILITAIRE";
        if(terrain.trenches.contains(x,y))return "LES TRANCHEES";
        if(terrain.village.contains(x,y))return "VILLAGE EN RUINE";
        if(terrain.forest.contains(x,y))return "FORET DU NORD";
        if(y>WATER_BOTTOM)return "BATTERIE DU SUD";
        return "LE BOCAGE";
    }

    public BufferedImage overview(){return overview;}
    public int entityCount(){return entities.size();}
    public List<MapEntity> getEntities(){return Collections.unmodifiableList(entities);}
    public void setChestListener(Consumer<String> listener){chestListener=listener==null?id->{}:listener;}
    public static Rectangle2D.Double playerBox(double x,double feetY){return new Rectangle2D.Double(x-11,feetY-17,22,17);}
    public boolean canMove(Rectangle2D box){
        if(box.getMinX()<0||box.getMinY()<0||box.getMaxX()>width||box.getMaxY()>height||water.intersects(box))return false;
        for(Rectangle2D wall:trenchWalls)if(wall.intersects(box))return false;
        for(int cy=(int)box.getMinY()/128;cy<=(int)box.getMaxY()/128;cy++)
            for(int cx=(int)box.getMinX()/128;cx<=(int)box.getMaxX()/128;cx++){
                List<MapEntity> local=cells.get(cy*64+cx);
                if(local!=null)for(MapEntity e:local)if(e.collision.intersects(box))return false;
            }
        return true;
    }
    public Point2D.Double move(double x,double y,double dx,double dy){
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/4));
        for(int i=0;i<steps;i++){
            if(canMove(playerBox(x+dx/steps,y)))x+=dx/steps;
            if(canMove(playerBox(x,y+dy/steps)))y+=dy/steps;
        }
        return new Point2D.Double(x,y);
    }
    public boolean blocksShot(double x1,double y1,double x2,double y2){
        for(Rectangle2D wall:trenchWalls)if(wall.intersectsLine(x1,y1,x2,y2))return true;
        for(MapEntity e:entities)if(e.collision!=null&&e.collision.intersectsLine(x1,y1,x2,y2))return true;
        return false;
    }
    public void updateTriggers(double x,double y){
        for(MapEntity e:entities)if(e.isChest()&&!e.opened&&Point2D.distance(x,y,e.x,e.y)<68){e.open();chestListener.accept(e.id);}
    }
    public void reset(){for(MapEntity e:entities)e.reset();}

    public void draw(Graphics2D g,double playerY,Runnable drawPlayer,boolean debug){
        sort();g.drawImage(ground,0,0,null);
        Rectangle view=g.getClipBounds();
        for(MapEntity e:entities)if(e.ground&&visible(e,view))e.draw(g);
        for(MapEntity e:entities)if(!e.ground&&visible(e,view)){
            int w=e.getType().width;
            g.setColor(new Color(15,24,19,e.getType().footprint.equals("tree")?48:32));
            g.fillOval((int)e.x-w/3,(int)e.y-8,w*3/4,17);
        }
        boolean drawn=false;
        for(MapEntity e:entities)if(!e.ground){
            if(!drawn&&playerY<e.y){drawPlayer.run();drawn=true;}
            if(visible(e,view))e.draw(g);
        }
        if(!drawn)drawPlayer.run();
        if(debug){
            g.setStroke(new BasicStroke(1));g.setColor(new Color(240,85,65,190));g.draw(water);
            for(Rectangle2D wall:trenchWalls)g.draw(wall);
            for(MapEntity e:entities)if(e.collision!=null&&visible(e,view))g.draw(e.collision);
        }
    }
    private boolean visible(MapEntity e,Rectangle view){return view==null||view.intersects(e.x-e.getType().width/2.0,e.y-e.getType().height,e.getType().width,e.getType().height+20);}
}
