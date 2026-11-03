import world.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public final class Main {
    public static void main(String[] args)throws Exception{
        final GameMap map=new GameMap();
        final BufferedImage sheet=Sprites.read("/assets/hero.png");
        final BufferedImage[] hero=new BufferedImage[4];
        int frameWidth=sheet.getWidth()/4;
        for(int i=0;i<hero.length;i++)hero[i]=Sprites.prepare(sheet.getSubimage(i*frameWidth,0,frameWidth,sheet.getHeight()),156,246);
        SwingUtilities.invokeLater(()->{
            GamePanel panel=new GamePanel(map,hero);
            JFrame window=new JFrame("Le Debarquement d'Hugo");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(panel);window.pack();window.setLocationRelativeTo(null);
            window.addWindowFocusListener(new WindowAdapter(){
                public void windowLostFocus(WindowEvent e){panel.keys.clear();panel.paused=true;}
            });
            window.setVisible(true);panel.requestFocusInWindow();panel.start();
        });
    }

    private static final class GamePanel extends JPanel{
        private static final long serialVersionUID=1L;
        final GameMap world;
        final BufferedImage[] hero;
        final Camera camera=new Camera();
        final Set<Integer> keys=new HashSet<>();
        double x=GameMap.SPAWN_X,y=GameMap.SPAWN_Y;
        long previous;
        boolean debug,paused,walking,facingLeft,miniMap=true;
        double walkTime;
        String message="";
        double messageTime;

        GamePanel(GameMap world,BufferedImage[] hero){
            this.world=world;this.hero=hero;setPreferredSize(new Dimension(1440,900));setFocusable(true);
            world.setChestListener(id->{message="Caisse HUGO ouverte";messageTime=3;});
            addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent e){
                    int key=e.getKeyCode();if(!keys.add(key))return;
                    if(key==KeyEvent.VK_F1)debug=!debug;
                    if(key==KeyEvent.VK_P||key==KeyEvent.VK_ESCAPE)paused=!paused;
                    if(key==KeyEvent.VK_M)miniMap=!miniMap;
                    if(key==KeyEvent.VK_ADD||key==KeyEvent.VK_PLUS||key==KeyEvent.VK_EQUALS)camera.zoom+=.15;
                    if(key==KeyEvent.VK_SUBTRACT||key==KeyEvent.VK_MINUS)camera.zoom-=.15;
                    if(key==KeyEvent.VK_R){x=GameMap.SPAWN_X;y=GameMap.SPAWN_Y;world.reset();camera.zoom=1.25;}
                }
                public void keyReleased(KeyEvent e){keys.remove(e.getKeyCode());}
            });
            addMouseWheelListener(e->{camera.zoom-=e.getPreciseWheelRotation()*.1;});
        }
        boolean held(int...codes){for(int code:codes)if(keys.contains(code))return true;return false;}
        void start(){previous=System.nanoTime();new Timer(16,e->tick()).start();}
        void tick(){
            long now=System.nanoTime();double dt=Math.min(.05,(now-previous)/1_000_000_000.0);previous=now;
            if(!paused){
                walking=false;
                double dx=(held(KeyEvent.VK_D,KeyEvent.VK_RIGHT)?1:0)-(held(KeyEvent.VK_Q,KeyEvent.VK_A,KeyEvent.VK_LEFT)?1:0);
                double dy=(held(KeyEvent.VK_S,KeyEvent.VK_DOWN)?1:0)-(held(KeyEvent.VK_Z,KeyEvent.VK_W,KeyEvent.VK_UP)?1:0);
                double length=Math.hypot(dx,dy);
                if(length>0){
                    Point2D.Double pos=world.move(x,y,dx/length*108*dt,dy/length*108*dt);
                    walking=Point2D.distance(x,y,pos.x,pos.y)>.01;
                    x=pos.x;y=pos.y;
                    if(dx!=0)facingLeft=dx<0;
                    if(walking)walkTime+=dt;
                }
                world.updateTriggers(x,y);messageTime=Math.max(0,messageTime-dt);
            }
            camera.follow(x,y,getWidth(),getHeight(),world.width,world.height);repaint();
        }
        protected void paintComponent(Graphics original){
            super.paintComponent(original);
            Graphics2D g=(Graphics2D)original.create();
            g.setClip(0,0,getWidth(),getHeight());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.translate(Math.rint(getWidth()/2.0-camera.x*camera.zoom),Math.rint(getHeight()/2.0-camera.y*camera.zoom));g.scale(camera.zoom,camera.zoom);
            world.draw(g,y,()->{
                g.setColor(new Color(15,24,18,80));g.fillOval((int)x-18,(int)y-8,38,13);
                BufferedImage frame=hero[walking?(int)(walkTime*7)%hero.length:0];
                g.drawImage(frame,(int)x+(facingLeft?26:-26),(int)y-82,facingLeft?-52:52,82,null);
                if(debug){g.setColor(Color.CYAN);g.draw(GameMap.playerBox(x,y));}
            },debug);g.dispose();
            drawHud((Graphics2D)original);
        }
        void drawHud(Graphics2D original){
            Graphics2D g=(Graphics2D)original.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0,0,new Color(8,16,12,160),0,150,new Color(8,16,12,0)));
            g.fillRect(0,0,getWidth(),150);
            g.setFont(new Font("SansSerif",Font.BOLD,14));g.setColor(new Color(220,206,157));
            g.drawString("LE DEBARQUEMENT D'HUGO",28,32);
            g.setFont(new Font("SansSerif",Font.BOLD,24));g.setColor(new Color(245,241,217));g.drawString(world.zoneAt(x,y),28,63);
            g.setColor(new Color(207,186,119));g.fillRect(28,75,45,2);
            if(miniMap){
                int mx=getWidth()-254,my=24;
                g.setColor(new Color(13,22,17,220));g.fillRoundRect(mx-7,my-7,244,174,8,8);
                g.drawImage(world.overview(),mx,my,null);
                g.setColor(new Color(220,206,157,150));g.drawRect(mx,my,230,160);
                int px=mx+(int)(x/world.width*230),py=my+(int)(y/world.height*160);
                g.setColor(new Color(21,30,19));g.fillOval(px-5,py-5,10,10);
                g.setColor(new Color(255,228,144));g.fillOval(px-3,py-3,6,6);
                g.setColor(new Color(255,255,230,100));
                g.drawRect(mx+(int)((camera.x-getWidth()/(2*camera.zoom))/world.width*230),my+(int)((camera.y-getHeight()/(2*camera.zoom))/world.height*160),(int)(getWidth()/camera.zoom/world.width*230),(int)(getHeight()/camera.zoom/world.height*160));
            }
            g.setFont(new Font("SansSerif",Font.PLAIN,12));g.setColor(new Color(241,239,213));
            String controls="ZQSD / Fleches : deplacement     Molette : zoom     M : carte     P : pause";
            g.setColor(new Color(15,22,17,170));g.fillRoundRect(20,getHeight()-43,530,26,7,7);
            g.setColor(new Color(226,226,202));g.drawString(controls,32,getHeight()-25);
            if(debug){g.setColor(Color.WHITE);g.drawString(world.entityCount()+" objets | "+(int)x+", "+(int)y+" | zoom "+String.format("%.2f",camera.zoom),28,102);}
            if(messageTime>0){g.setFont(new Font("SansSerif",Font.BOLD,18));g.setColor(new Color(249,225,163));g.drawString(message,getWidth()/2-100,getHeight()-75);}
            if(paused){
                g.setColor(new Color(9,17,13,135));g.fillRect(0,0,getWidth(),getHeight());
                g.setFont(new Font("SansSerif",Font.BOLD,32));g.setColor(new Color(245,235,200));
                g.drawString("PAUSE",getWidth()/2-58,getHeight()/2-8);
                g.setFont(new Font("SansSerif",Font.PLAIN,16));g.drawString("P pour reprendre",getWidth()/2-63,getHeight()/2+26);
            }
            g.dispose();
        }
    }
}
