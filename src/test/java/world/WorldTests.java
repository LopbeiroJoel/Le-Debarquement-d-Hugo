package world;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.EnumSet;
import javax.imageio.ImageIO;

public final class WorldTests {
    static void check(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    public static void main(String[] args)throws Exception{
        GameMap world=new GameMap();
        check(world.width==4608&&world.height==3200,"dimensions agrandies");
        check(world.canMove(GameMap.playerBox(GameMap.SPAWN_X,GameMap.SPAWN_Y)),"depart libre");
        check(!world.canMove(GameMap.playerBox(0,900)),"limite gauche");
        check(!world.canMove(GameMap.playerBox(world.width,900)),"limite droite");
        check(!world.canMove(GameMap.playerBox(900,0)),"limite haute");
        check(!world.canMove(GameMap.playerBox(900,world.height+1)),"limite basse");
        check(!world.canMove(GameMap.playerBox(650,2400)),"riviere bloquante");
        for(int x:TerrainPainter.BRIDGES){
            check(world.canMove(GameMap.playerBox(x,2400)),"pont libre");
            Point2D.Double p=world.move(x,2300,0,250);check(Math.abs(p.y-2550)<.001,"traversee complete du pont "+x+" : "+p.y);
        }
        EnumSet<AssetType> used=EnumSet.noneOf(AssetType.class);
        int colliders=0,chests=0;
        MapEntity fence=null,firstChest=null;
        for(MapEntity e:world.getEntities()){
            used.add(e.getType());
            if(e.getType().footprint.equals("tree")||e.getType()==AssetType.BUSH)check(!world.overlapsRoad(e),"aucune vegetation sur les routes "+e.id);
            if(e.collision!=null){colliders++;Rectangle2D b=e.collision;check(!world.canMove(new Rectangle2D.Double(b.getCenterX()-1,b.getCenterY()-1,2,2)),"collision indexee "+e.id);}
            if(e.getType()==AssetType.FENCE_LONG)fence=e;
            if(e.isChest()){chests++;firstChest=e;}
        }
        for(AssetType type:AssetType.values())if(type!=AssetType.OPEN_CHEST)check(used.contains(type),"asset present "+type);
        check(world.entityCount()>600,"monde enrichi");
        check(world.canMove(GameMap.playerBox(650,900)),"acces arbre Joel");
        check(world.canMove(GameMap.playerBox(950,900)),"acces arbre Thomas");
        Rectangle2D f=fence.collision;
        Point2D.Double p=world.move(f.getCenterX(),f.getMaxY()+55,0,-220);
        check(p.y>=f.getMaxY(),"pas de traversee a grande vitesse");
        check(world.blocksShot(f.getMinX()-2,f.getCenterY(),f.getMaxX()+2,f.getCenterY()),"barriere bloque tir");
        boolean[] reach=reachable(world);
        int cols=world.width/16+1;
        check(reach[Math.round(896/16f)*cols+Math.round(656/16f)],"clairiere Joel reliee au depart");
        check(reach[Math.round(896/16f)*cols+Math.round(944/16f)],"clairiere Thomas reliee au depart");
        for(MapEntity e:world.getEntities())if(e.isChest()){
            boolean found=false;
            for(int y=(int)e.y-64;y<=e.y+64;y+=8)for(int x=(int)e.x-64;x<=e.x+64;x+=8){
                int cx=Math.round(x/16f),cy=Math.round(y/16f);
                if(Point2D.distance(cx*16,cy*16,e.x,e.y)<65&&reach[cy*cols+cx])found=true;
            }
            check(found,"coffre accessible depuis le depart "+e.id);
        }
        final int[] rewards={0};world.setChestListener(id->rewards[0]++);
        for(MapEntity e:world.getEntities())if(e.isChest()){world.updateTriggers(e.x,e.y+45);world.updateTriggers(e.x,e.y+45);}
        check(rewards[0]==chests,"un evenement par coffre");
        world.reset();world.updateTriggers(firstChest.x,firstChest.y+45);check(rewards[0]==chests+1,"reset des coffres");
        Camera camera=new Camera();camera.follow(-100,-100,1440,900,world.width,world.height);
        check(camera.x-1440/(2*camera.zoom)>=0&&camera.y-900/(2*camera.zoom)>=0,"camera haut gauche");
        camera.follow(99999,99999,1440,900,world.width,world.height);
        check(camera.x+1440/(2*camera.zoom)<=world.width&&camera.y+900/(2*camera.zoom)<=world.height,"camera bas droite");
        check("BASE MILITAIRE".equals(world.zoneAt(3100,800)),"zone base");
        check("LES TRANCHEES".equals(world.zoneAt(3900,1900)),"zone tranchees");
        check("FORET DU NORD".equals(world.zoneAt(500,400)),"zone foret");
        Sprites sprites=new Sprites();check(sprites.get(AssetType.RUIN).getWidth()==AssetType.RUIN.width*3,"details conserves dans le cache");
        if(args.length>0){
            world.reset();
            new File(args[0]).mkdirs();
            BufferedImage full=new BufferedImage(world.width,world.height,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=full.createGraphics();world.draw(g,0,()->{},false);g.dispose();ImageIO.write(full,"png",new File(args[0],"map-complete.png"));
            double[][] views={{2200,1500},{3100,850},{3900,1850},{900,1500},{850,600},{1250,2420},{800,850}};
            String[] names={"depart","base","tranchees","village","foret","pont","clairiere"};
            BufferedImage sheet=Sprites.read("/assets/hero.png");
            BufferedImage hero=Sprites.prepare(sheet.getSubimage(0,0,sheet.getWidth()/4,sheet.getHeight()),156,246);
            for(int i=0;i<views.length;i++){
                BufferedImage view=new BufferedImage(1440,900,BufferedImage.TYPE_INT_RGB);
                Graphics2D v=view.createGraphics();v.setClip(0,0,1440,900);v.scale(1.25,1.25);v.translate(576-views[i][0],360-views[i][1]);
                final double px=views[i][0],py=views[i][1];
                world.draw(v,py,()->v.drawImage(hero,(int)px-26,(int)py-82,52,82,null),false);
                v.dispose();ImageIO.write(view,"png",new File(args[0],names[i]+".png"));
            }
        }
        System.out.println("OK : "+world.width+"x"+world.height+", "+world.entityCount()+" objets, "+colliders+" collisions, "+chests+" coffres accessibles, 2 ponts, 6 zones, rendu detaille.");
    }
    private static boolean[] reachable(GameMap world){
        int step=16,cols=world.width/step+1,rows=world.height/step+1;
        boolean[] seen=new boolean[cols*rows];ArrayDeque<Integer> queue=new ArrayDeque<>();
        int start=(int)Math.round(GameMap.SPAWN_Y/step)*cols+(int)Math.round(GameMap.SPAWN_X/step);
        seen[start]=true;queue.add(start);int[] dx={1,-1,0,0},dy={0,0,1,-1};
        while(!queue.isEmpty()){
            int key=queue.remove(),x=key%cols,y=key/cols;
            for(int i=0;i<4;i++){
                int nx=x+dx[i],ny=y+dy[i];if(nx<0||ny<0||nx>=cols||ny>=rows)continue;
                int n=ny*cols+nx;
                if(!seen[n]&&world.canMove(GameMap.playerBox(nx*step,ny*step))){seen[n]=true;queue.add(n);}
            }
        }
        return seen;
    }
}
