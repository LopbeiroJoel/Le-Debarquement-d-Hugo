package hugo.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayDeque;
import java.util.EnumSet;

public final class TestsMonde {
    private static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
    public static void main(String[] args)throws Exception{
        CarteJeu map=new CarteJeu();
        check(map.width==4608&&map.height==3200,"dimensions conservees");
        check(map.entityCount()==660,"objets charges apres allegement");
        check(map.canMove(CarteJeu.playerBox(CarteJeu.SPAWN_X,CarteJeu.SPAWN_Y)),"depart libre");
        check(!map.canMove(CarteJeu.playerBox(0,500)),"bord gauche");
        check(!map.canMove(CarteJeu.playerBox(map.width,500)),"bord droit");
        check(!map.canMove(CarteJeu.playerBox(500,0)),"bord haut");
        check(!map.canMove(CarteJeu.playerBox(500,map.height+1)),"bord bas");
        check(!map.canMove(CarteJeu.playerBox(650,2400)),"eau bloquante");
        for(int x:CarteJeu.BRIDGES){Vector2 p=map.move(x,2300,0,250);check(Math.abs(p.y-2550)<.02,"pont traversable "+x);}
        EnumSet<TypeDecor> used=EnumSet.noneOf(TypeDecor.class);int chests=0,collisions=0,bushes=0;float previous=-1;
        for(ObjetDecor e:map.getEntities()){
            check(e.y>=previous,"ordre de profondeur");previous=e.y;used.add(e.getType());
            if(e.getType().footprint.equals("tree")||e.getType()==TypeDecor.BUSH)check(!map.overlapsRoad(e),"vegetation hors des routes "+e.id);
            if(e.getType()==TypeDecor.BUSH)bushes++;
            if(e.collision!=null){
                collisions++;Rectangle r=e.collision;
                Rectangle roadClearance=new Rectangle(r.x-24,r.y-24,r.width+48,r.height+48);
                check(!map.areas.get("roads").intersects(roadClearance),"passage libre sur la route "+e.id);
                check(!map.canMove(new Rectangle(r.x+r.width/2-1,r.y+r.height/2-1,2,2)),"collision "+e.id);
            }
            if(e.getType()==TypeDecor.FENCE_LONG){
                Rectangle r=e.collision;Vector2 stop=map.move(e.x,r.y+r.height+55,0,-220);
                check(stop.y>=r.y+r.height,"aucune traversee de barriere");
                check(map.blocksShot(r.x-5,r.y+r.height/2,r.x+r.width+5,r.y+r.height/2),"tirs bloques");
            }
            if(e.isChest())chests++;
        }
        check(bushes==178,"moitie des buissons conserves");
        for(TypeDecor type:TypeDecor.values())if(type!=TypeDecor.OPEN_CHEST)check(used.contains(type),"asset conserve "+type);
        boolean[] reach=reachable(map);int cols=map.width/16+1;
        for(ObjetDecor e:map.getEntities())if(e.isChest()){
            boolean found=false;
            for(int y=(int)e.y-64;y<=e.y+64;y+=8)for(int x=(int)e.x-64;x<=e.x+64;x+=8){
                int cx=Math.round(x/16f),cy=Math.round(y/16f);
                if(Vector2.dst(cx*16,cy*16,e.x,e.y)<65&&reach[cy*cols+cx])found=true;
            }
            check(found,"coffre accessible "+e.id);
        }
        check(reach[56*cols+41]&&reach[56*cols+59],"clairiere accessible");
        final int[] rewards={0};map.setChestListener(id->rewards[0]++);
        ObjetDecor first=null;
        for(ObjetDecor e:map.getEntities())if(e.isChest()){first=e;map.updateTriggers(e.x,e.y+45);map.updateChests(0.5f);map.updateTriggers(e.x,e.y+45);}
        check(rewards[0]==chests&&chests==6,"une ouverture par caisse");
        map.updateTriggers(first.x,first.y+45);
        map.updateChests(110f);
        check(first.getType()==TypeDecor.OPEN_CHEST,"coffre encore ouvert");
        map.updateChests(11f);
        check(first.getType()==TypeDecor.CHEST,"coffre referme apres deux minutes");
        map.updateTriggers(first.x,first.y+45);
        map.updateChests(0.5f);
        check(rewards[0]==chests,"pas de reouverture sans s'eloigner");
        map.updateTriggers(CarteJeu.SPAWN_X,CarteJeu.SPAWN_Y);
        map.updateTriggers(first.x,first.y+45);
        map.updateChests(0.5f);
        check(rewards[0]==chests+1,"coffre reutilisable");
        map.reset();map.updateTriggers(first.x,first.y+45);map.updateChests(0.5f);
        check(rewards[0]==chests+2,"reset des caisses");
        check("BASE MILITAIRE".equals(map.zoneAt(3100,800)),"base conservee");
        check("LES TRANCHEES".equals(map.zoneAt(3900,1900)),"tranchees conservees");
        check("FORET DU NORD".equals(map.zoneAt(500,400)),"foret conservee");
        CameraCarte camera=new CameraCarte();camera.follow(-100,-100,1440,900,map.width,map.height);
        check(camera.x-1440/(2*camera.zoom)>=0&&camera.y-900/(2*camera.zoom)>=0,"limites camera");
        camera.follow(99999,99999,1440,900,map.width,map.height);
        check(camera.x+1440/(2*camera.zoom)<=map.width&&camera.y+900/(2*camera.zoom)<=map.height,"limites camera opposees");
        ZoneCarte water=map.areas.get("riverShape");check(water.contains(650,2400)&&!water.contains(650,2000),"geometrie riviere");
        System.out.println("OK libGDX : "+map.entityCount()+" objets, "+collisions+" collisions, 6 caisses accessibles, 2 ponts, routes degagees, camera et zones");
    }
    private static boolean[] reachable(CarteJeu map){
        int cols=map.width/16+1,rows=map.height/16+1;boolean[] seen=new boolean[cols*rows];
        ArrayDeque<Integer> queue=new ArrayDeque<>();int start=Math.round(CarteJeu.SPAWN_Y/16)*cols+Math.round(CarteJeu.SPAWN_X/16);
        queue.add(start);seen[start]=true;int[] dx={1,-1,0,0},dy={0,0,1,-1};
        while(!queue.isEmpty()){
            int key=queue.remove(),x=key%cols,y=key/cols;
            for(int i=0;i<4;i++){
                int nx=x+dx[i],ny=y+dy[i];if(nx<0||ny<0||nx>=cols||ny>=rows)continue;
                int n=ny*cols+nx;if(!seen[n]&&map.canMove(CarteJeu.playerBox(nx*16,ny*16))){seen[n]=true;queue.add(n);}
            }
        }
        return seen;
    }
}
