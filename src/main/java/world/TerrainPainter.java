package world;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public final class TerrainPainter {
    public static final int WIDTH=4608, HEIGHT=3200;
    public static final int RIVER_TOP=2330, RIVER_BOTTOM=2510;
    public static final int[] BRIDGES={1250,3420};
    public final Area roads=new Area();
    public final Area forest=new Area(new Ellipse2D.Double(-500,-400,2650,2050));
    public final Area base=new Area(new RoundRectangle2D.Double(2580,400,1080,790,180,180));
    public final Area village=new Area(new Ellipse2D.Double(420,1030,1280,1050));
    public final Area trenches=new Area(new RoundRectangle2D.Double(3260,1520,1190,710,150,150));
    public final Area battery=new Area(new Ellipse2D.Double(2090,2630,1490,680));
    public final Path2D riverShape=new Path2D.Double();
    private final Random random=new Random(194411);
    private final Path2D mainRoad=new Path2D.Double();
    private final Path2D branches=new Path2D.Double();

    public TerrainPainter(){
        riverShape.moveTo(0,RIVER_TOP+riverOffset(0));
        for(int x=16;x<=WIDTH;x+=16)riverShape.lineTo(x,RIVER_TOP+riverOffset(x));
        riverShape.lineTo(WIDTH,RIVER_BOTTOM+riverOffset(WIDTH));
        for(int x=WIDTH-16;x>=0;x-=16)riverShape.lineTo(x,RIVER_BOTTOM+riverOffset(x));
        riverShape.closePath();
        mainRoad.moveTo(-80,1740);
        mainRoad.curveTo(700,1670,1100,1340,1610,1440);
        mainRoad.curveTo(2300,1590,2780,1320,3300,1360);
        mainRoad.curveTo(3830,1400,4200,1230,4700,1350);
        branches.moveTo(3100,1430);branches.curveTo(3110,1200,3100,900,3100,610);
        branches.moveTo(1340,1470);branches.curveTo(1290,1850,1250,2110,1250,2600);
        branches.curveTo(1450,2920,1900,2780,2530,2910);
        branches.curveTo(3080,3050,3470,2780,3420,2550);
        branches.lineTo(3420,2160);branches.curveTo(3290,1940,3090,1790,2890,1490);
        branches.moveTo(1900,1470);branches.curveTo(1700,1160,1400,920,1130,750);
        branches.curveTo(1170,910,1100,1080,800,1080);
        roads.add(new Area(new BasicStroke(126,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND).createStrokedShape(mainRoad)));
        roads.add(new Area(new BasicStroke(98,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND).createStrokedShape(branches)));
    }

    public BufferedImage paint()throws IOException{
        BufferedImage result=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=result.createGraphics();
        BufferedImage grass=Sprites.read("/assets/terrain-v2.png");
        for(int y=0;y<HEIGHT;y+=768)for(int x=0;x<WIDTH;x+=768)g.drawImage(grass,x,y,768,768,null);
        patch(g,forest,new Color(43,57,35,160),new Color(95,82,48,100),8500);
        patch(g,village,new Color(112,107,91,205),new Color(152,143,117,125),6000);
        patch(g,base,new Color(123,118,101,235),new Color(166,155,128,135),6500);
        patch(g,trenches,new Color(73,57,42,235),new Color(110,91,62,160),6500);
        patch(g,battery,new Color(119,104,66,155),new Color(152,129,86,125),4000);
        road(g,mainRoad,106);
        road(g,branches,76);
        drawTrenches(g);
        drawRiver(g);
        drawGroundDetails(g);
        g.dispose();
        return result;
    }

    private void patch(Graphics2D g,Shape area,Color fill,Color grain,int count){
        Rectangle bounds=area.getBounds();
        BufferedImage wash=new BufferedImage(bounds.width,bounds.height,BufferedImage.TYPE_INT_ARGB);
        int rgb=fill.getRGB()&0xffffff;
        for(int y=0;y<bounds.height;y+=2)for(int x=0;x<bounds.width;x+=2){
            int wx=bounds.x+x,wy=bounds.y+y;
            if(!area.contains(wx,wy))continue;
            double opacity=area.contains(wx-40,wy-40,80,80)?1:area.contains(wx-24,wy-24,48,48)?.72:area.contains(wx-12,wy-12,24,24)?.4:.14;
            int pixel=((int)(fill.getAlpha()*opacity)<<24)|rgb;
            wash.setRGB(x,y,pixel);if(x+1<bounds.width)wash.setRGB(x+1,y,pixel);
            if(y+1<bounds.height){wash.setRGB(x,y+1,pixel);if(x+1<bounds.width)wash.setRGB(x+1,y+1,pixel);}
        }
        g.drawImage(wash,bounds.x,bounds.y,null);
        Graphics2D p=(Graphics2D)g.create();p.clip(area);
        for(int i=0;i<count;i++){
            int x=bounds.x+random.nextInt(bounds.width),y=bounds.y+random.nextInt(bounds.height);
            p.setColor(i%3==0?grain:new Color(34,43,27,25));
            p.fillRect(x,y,2+random.nextInt(9),1+random.nextInt(4));
        }
        p.dispose();
        Graphics2D edge=(Graphics2D)g.create();
        for(int i=0;i<2600;i++){
            int x=bounds.x+random.nextInt(bounds.width),y=bounds.y+random.nextInt(bounds.height);
            if(area.contains(x,y)&&!area.contains(x-24,y-24,48,48)){
                edge.setColor(new Color(fill.getRed(),fill.getGreen(),fill.getBlue(),40));
                edge.fillOval(x-18,y-8,36,16);
            }
        }
        edge.dispose();
    }

    private void road(Graphics2D g,Path2D path,int width){
        for(int extra=24;extra>=0;extra-=6){
            g.setColor(new Color(92,83,53,extra==0?160:26));
            g.setStroke(new BasicStroke(width+extra,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.draw(path);
        }
        Shape surface=new BasicStroke(width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND).createStrokedShape(path);
        BufferedImage dirt=new BufferedImage(192,192,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<192;y++)for(int x=0;x<192;x++){
            int noise=random.nextInt(21)-10;
            int broad=(int)(Math.sin(x*.13+y*.09)*4);
            dirt.setRGB(x,y,new Color(145+noise+broad,126+noise+broad,89+noise+broad).getRGB());
        }
        Graphics2D p=(Graphics2D)g.create();p.clip(surface);
        p.setPaint(new TexturePaint(dirt,new Rectangle(0,0,192,192)));p.fill(surface);
        p.setColor(new Color(81,67,45,45));
        p.setStroke(new BasicStroke(2));
        for(int offset=-28;offset<=28;offset+=56){
            Graphics2D track=(Graphics2D)p.create();if(path==mainRoad)track.translate(0,offset);else track.translate(offset,0);track.draw(path);track.dispose();
        }
        for(int i=0;i<24000;i++){
            int x=random.nextInt(WIDTH),y=random.nextInt(HEIGHT);
            if(surface.contains(x,y)){
                p.setColor(i%3==0?new Color(64,62,47,115):new Color(192,175,135,125));
                p.fillRect(x,y,2+random.nextInt(4),1+random.nextInt(2));
            }
        }
        p.dispose();
        Area verge=new Area(new BasicStroke(width+12,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND).createStrokedShape(path));
        verge.subtract(new Area(new BasicStroke(width-8,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND).createStrokedShape(path)));
        g.setColor(new Color(108,111,65,130));
        for(int i=0;i<75000;i++){
            int x=random.nextInt(WIDTH),y=random.nextInt(HEIGHT);
            if(verge.contains(x,y))g.fillRect(x,y,3+random.nextInt(6),2+random.nextInt(3));
        }
    }

    private void drawTrenches(Graphics2D g){
        Path2D channel=new Path2D.Double();
        channel.moveTo(3340,1660);channel.lineTo(4210,1660);channel.lineTo(4210,1850);
        channel.lineTo(3520,1850);channel.lineTo(3520,2050);channel.lineTo(4300,2050);
        g.setStroke(new BasicStroke(74,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));g.setColor(new Color(127,108,72));g.draw(channel);
        g.setStroke(new BasicStroke(54,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));g.setColor(new Color(48,43,32));g.draw(channel);
        g.setStroke(new BasicStroke(35,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));g.setColor(new Color(82,68,47));g.draw(channel);
        g.setColor(new Color(126,105,71));
        for(int x=3350;x<4200;x+=18){g.fillRect(x,1642,5,34);g.fillRect(x+180,2032,5,34);}
        for(int x=3540;x<4210;x+=18)g.fillRect(x,1832,5,34);
        g.setColor(new Color(35,40,32,75));
        for(int i=0;i<130;i++)g.fillOval(3350+random.nextInt(940),1610+random.nextInt(490),5+random.nextInt(17),3+random.nextInt(7));
    }

    private void drawRiver(Graphics2D g){
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(79,83,50));g.setStroke(new BasicStroke(40));g.draw(riverShape);
        g.setColor(new Color(115,107,72));g.setStroke(new BasicStroke(15));g.draw(riverShape);
        g.setStroke(new BasicStroke(1));
        g.setPaint(new GradientPaint(0,RIVER_TOP,new Color(48,73,65),0,RIVER_BOTTOM,new Color(69,100,86)));
        g.fill(riverShape);
        for(int i=0;i<3800;i++){
            int x=random.nextInt(WIDTH),y=RIVER_TOP+(int)riverOffset(x)+random.nextInt(RIVER_BOTTOM-RIVER_TOP);
            g.setColor(i%3==0?new Color(152,175,137,75):new Color(32,61,58,75));
            g.fillRect(x,y,3+random.nextInt(18),1+random.nextInt(2));
        }
        for(int i=0;i<900;i++){
            int x=random.nextInt(WIDTH),y=(i%2==0?RIVER_TOP-15:RIVER_BOTTOM+5)+(int)riverOffset(x)+random.nextInt(20);
            boolean bridge=false;for(int center:BRIDGES)if(Math.abs(center-x)<100)bridge=true;
            if(bridge)continue;
            g.setColor(i%3==0?new Color(143,139,75):new Color(70,89,47));
            g.drawLine(x,y,x-2,y-5-random.nextInt(12));g.drawLine(x+2,y,x+4,y-7);
        }
    }

    private double riverOffset(int x){
        double wave=19*Math.sin(x/290.0)+8*Math.sin(x/112.0);
        for(int bridge:BRIDGES)wave*=Math.min(1,Math.max(0,Math.abs(x-bridge)-90)/90.0);
        return wave;
    }

    private void drawGroundDetails(Graphics2D g){
        g.setStroke(new BasicStroke(1));
        for(int i=0;i<22000;i++){
            int x=random.nextInt(WIDTH),y=random.nextInt(HEIGHT);
            if(y>RIVER_TOP-25&&y<RIVER_BOTTOM+30||roads.contains(x,y)||trenches.contains(x,y)||base.contains(x,y))continue;
            boolean dark=forest.contains(x,y);
            g.setColor(dark?new Color(31,49,26,100):new Color(51,80,38,115));
            g.drawLine(x,y,x-2,y-4);g.drawLine(x+1,y,x+3,y-6);
            g.setColor(dark?new Color(102,108,59,130):new Color(135,151,77,135));
            g.drawLine(x,y,x,y-5);
            if(i%13==0){g.setColor(new Color(186,176,115,145));g.fillRect(x+3,y-4,2,2);}
            if(i%7==0){g.setColor(new Color(89,91,70));g.fillRect(x+7,y+2,5,3);g.setColor(new Color(137,137,108));g.fillRect(x+7,y+2,3,1);}
        }
    }
}
