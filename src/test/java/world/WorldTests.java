package world;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.EnumSet;
import javax.imageio.ImageIO;


public final class WorldTests{
    public static void main(String[] args)throws Exception{run(args.length>0?args[0]:null);}
    static void check(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    public static void run(String preview)throws Exception{
        GameMap world=new GameMap();
        check(world.canMove(GameMap.playerBox(GameMap.SPAWN_X,GameMap.SPAWN_Y)),"depart accessible");
        check(!world.canMove(GameMap.playerBox(0,100)),"limite gauche");
        check(!world.canMove(GameMap.playerBox(world.width,100)),"limite droite");
        check(!world.canMove(GameMap.playerBox(700,-1)),"limite haute");
        check(!world.canMove(GameMap.playerBox(700,world.height+1)),"limite basse");
        check(!world.canMove(GameMap.playerBox(650,890)),"eau bloquante");
        check(world.canMove(GameMap.playerBox(742,890)),"pont traversable");
        EnumSet<AssetType> used=EnumSet.noneOf(AssetType.class);
        int solids=0;
        for(MapEntity e:world.getEntities()){
            used.add(e.getType());
            if(e.collision!=null){solids++;check(!world.canMove(new Rectangle2D.Double(e.collision.getCenterX()-1,e.collision.getCenterY()-1,2,2)),"collision "+e.id);}
        }
        for(AssetType t:AssetType.values())if(t!=AssetType.OPEN_CHEST)check(used.contains(t),"asset place : "+t);
        MapEntity fence=null;
        for(MapEntity e:world.getEntities())if(e.getType()==AssetType.FENCE_LONG){fence=e;break;}
        Rectangle2D f=fence.collision;
        Point2D.Double stop=world.move(f.getCenterX(),f.getMaxY()+35,0,-100);
        check(stop.y>=f.getMaxY(),"aucune traversee de barriere lors d'un gros deplacement");
        check(world.blocksShot(f.getMinX()-5,f.getCenterY(),f.getMaxX()+5,f.getCenterY()),"les barrieres bloquent les tirs");
        check(!world.blocksShot(500,890,600,890),"eau ne bloque pas les tirs");
        final int[] count={0};world.setChestListener(id->count[0]++);
        for(MapEntity e:world.getEntities())if(e.isChest()){
            check(reachable(world,e.x,e.y),"coffre accessible "+e.id);
            world.updateTriggers(e.x,e.y+15);world.updateTriggers(e.x,e.y+15);
        }
        check(count[0]==2,"une seule recompense par coffre");
        world.reset();world.updateTriggers(320,380);check(count[0]==3,"reset reactualise le coffre");
        Camera camera=new Camera();camera.follow(-100,-100,1280,720,world.width,world.height);
        check(camera.x==320&&camera.y==180,"camera limite haute gauche");
        camera.follow(99999,99999,1280,720,world.width,world.height);
        check(camera.x==world.width-320&&camera.y==world.height-180,"camera limite basse droite");
        if(preview!=null){
            world.reset();BufferedImage im=new BufferedImage(world.width,world.height,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=im.createGraphics();world.draw(g,GameMap.SPAWN_Y,()->{
                g.setColor(Color.YELLOW);g.fillRect((int)GameMap.SPAWN_X-6,(int)GameMap.SPAWN_Y-23,12,23);
            },false);g.dispose();ImageIO.write(im,"png",new File(preview));
        }
        System.out.println("OK : "+world.entityCount()+" entites, "+solids+" collisions, 39 decors utilises, pont, camera, coffres accessibles et reset.");
    }
    private static boolean reachable(GameMap w,double targetX,double targetY){
        int step=8,cols=w.width/step,rows=w.height/step;
        boolean[] seen=new boolean[cols*rows];ArrayDeque<Integer> q=new ArrayDeque<>();
        int start=(int)Math.round(GameMap.SPAWN_Y/step)*cols+(int)Math.round(GameMap.SPAWN_X/step);
        seen[start]=true;q.add(start);
        int[] dx={1,-1,0,0},dy={0,0,1,-1};
        while(!q.isEmpty()){
            int key=q.remove(),x=key%cols,y=key/cols;
            if(Point2D.distance(x*step,y*step,targetX,targetY)<27)return true;
            for(int i=0;i<4;i++){
                int nx=x+dx[i],ny=y+dy[i];if(nx<0||ny<0||nx>=cols||ny>=rows)continue;
                int n=ny*cols+nx;if(seen[n]||!w.canMove(GameMap.playerBox(nx*step,ny*step)))continue;
                seen[n]=true;q.add(n);
            }
        }return false;
    }
}
