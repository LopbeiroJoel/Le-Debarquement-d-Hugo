package hugo.epitech.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayDeque;
import java.util.EnumSet;

public final class WorldTests {
    private static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
    public static void main(String[] args)throws Exception{
        GameMap map=new GameMap();
        check(map.width==4608&&map.height==3200,"dimensions conservees");
        check(map.entityCount()==854,"objets charges et palissades laterales retirees");
        check(map.canMove(GameMap.playerBox(GameMap.SPAWN_X,GameMap.SPAWN_Y)),"depart libre");
        check(!map.canMove(GameMap.playerBox(0,500)),"bord gauche");
        check(!map.canMove(GameMap.playerBox(map.width,500)),"bord droit");
        check(!map.canMove(GameMap.playerBox(500,0)),"bord haut");
        check(!map.canMove(GameMap.playerBox(500,map.height+1)),"bord bas");
        check(!map.canMove(GameMap.playerBox(650,2400)),"eau bloquante");
        for(int x:GameMap.BRIDGES){Vector2 p=map.move(x,2300,0,250);check(Math.abs(p.y-2550)<.02,"pont traversable "+x);}
        EnumSet<AssetType> used=EnumSet.noneOf(AssetType.class);int chests=0,collisions=0;float previous=-1;
        for(MapEntity e:map.getEntities()){
            check(e.y>=previous,"ordre de profondeur");previous=e.y;used.add(e.getType());
            if(e.getType().footprint.equals("tree")||e.getType()==AssetType.BUSH)check(!map.overlapsRoad(e),"vegetation hors des routes "+e.id);
            if(e.collision!=null){
                collisions++;Rectangle r=e.collision;
                check(!map.canMove(new Rectangle(r.x+r.width/2-1,r.y+r.height/2-1,2,2)),"collision "+e.id);
            }
            if(e.getType()==AssetType.FENCE_LONG){
                Rectangle r=e.collision;Vector2 stop=map.move(e.x,r.y+r.height+55,0,-220);
                check(stop.y>=r.y+r.height,"aucune traversee de barriere");
                check(map.blocksShot(r.x-5,r.y+r.height/2,r.x+r.width+5,r.y+r.height/2),"tirs bloques");
            }
            if(e.isChest())chests++;
        }
        for(AssetType type:AssetType.values())if(type!=AssetType.OPEN_CHEST)check(used.contains(type),"asset conserve "+type);
        boolean[] reach=reachable(map);int cols=map.width/16+1;
        for(MapEntity e:map.getEntities())if(e.isChest()){
            boolean found=false;
            for(int y=(int)e.y-64;y<=e.y+64;y+=8)for(int x=(int)e.x-64;x<=e.x+64;x+=8){
                int cx=Math.round(x/16f),cy=Math.round(y/16f);
                if(Vector2.dst(cx*16,cy*16,e.x,e.y)<65&&reach[cy*cols+cx])found=true;
            }
            check(found,"coffre accessible "+e.id);
        }
        check(reach[56*cols+41]&&reach[56*cols+59],"clairiere accessible");
        final int[] rewards={0};map.setChestListener(id->rewards[0]++);
        MapEntity first=null;
        for(MapEntity e:map.getEntities())if(e.isChest()){first=e;map.updateTriggers(e.x,e.y+45);map.updateTriggers(e.x,e.y+45);}
        check(rewards[0]==chests&&chests==6,"une ouverture par caisse");
        map.reset();map.updateTriggers(first.x,first.y+45);check(rewards[0]==chests+1,"reset des caisses");
        check("BASE MILITAIRE".equals(map.zoneAt(3100,800)),"base conservee");
        check("LES TRANCHEES".equals(map.zoneAt(3900,1900)),"tranchees conservees");
        check("FORET DU NORD".equals(map.zoneAt(500,400)),"foret conservee");
        Camera camera=new Camera();camera.follow(-100,-100,1440,900,map.width,map.height);
        check(camera.x-1440/(2*camera.zoom)>=0&&camera.y-900/(2*camera.zoom)>=0,"limites camera");
        camera.follow(99999,99999,1440,900,map.width,map.height);
        check(camera.x+1440/(2*camera.zoom)<=map.width&&camera.y+900/(2*camera.zoom)<=map.height,"limites camera opposees");
        MapArea water=map.areas.get("riverShape");check(water.contains(650,2400)&&!water.contains(650,2000),"geometrie riviere");
        System.out.println("OK libGDX : "+map.entityCount()+" objets, "+collisions+" collisions, 6 caisses accessibles, 2 ponts, routes degagees, camera et zones");
    }
    private static boolean[] reachable(GameMap map){
        int cols=map.width/16+1,rows=map.height/16+1;boolean[] seen=new boolean[cols*rows];
        ArrayDeque<Integer> queue=new ArrayDeque<>();int start=Math.round(GameMap.SPAWN_Y/16)*cols+Math.round(GameMap.SPAWN_X/16);
        queue.add(start);seen[start]=true;int[] dx={1,-1,0,0},dy={0,0,1,-1};
        while(!queue.isEmpty()){
            int key=queue.remove(),x=key%cols,y=key/cols;
            for(int i=0;i<4;i++){
                int nx=x+dx[i],ny=y+dy[i];if(nx<0||ny<0||nx>=cols||ny>=rows)continue;
                int n=ny*cols+nx;if(!seen[n]&&map.canMove(GameMap.playerBox(nx*16,ny*16))){seen[n]=true;queue.add(n);}
            }
        }
        return seen;
    }
}
