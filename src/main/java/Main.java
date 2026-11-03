import world.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public final class Main {
    public static void main(String[] args)throws Exception{
        final GameMap map=new GameMap();
        SwingUtilities.invokeLater(()->{
            MapPanel panel=new MapPanel(map);
            JFrame window=new JFrame("Le Debarquement d'Hugo");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(panel);window.pack();window.setLocationRelativeTo(null);
            window.addWindowFocusListener(new WindowAdapter(){
                public void windowLostFocus(WindowEvent e){panel.keys.clear();}
            });
            window.setVisible(true);panel.requestFocusInWindow();panel.start();
        });
    }

    private static final class MapPanel extends JPanel{
        private static final long serialVersionUID=1L;
        final GameMap world;
        final Camera camera=new Camera();
        final Set<Integer> keys=new HashSet<>();
        double viewX=GameMap.SPAWN_X,viewY=GameMap.SPAWN_Y;
        long previous;
        boolean debug,miniMap=true;

        MapPanel(GameMap world){
            this.world=world;setPreferredSize(new Dimension(1440,900));setFocusable(true);
            addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent e){
                    int key=e.getKeyCode();if(!keys.add(key))return;
                    if(key==KeyEvent.VK_F1)debug=!debug;
                    if(key==KeyEvent.VK_M)miniMap=!miniMap;
                    if(key==KeyEvent.VK_ADD||key==KeyEvent.VK_PLUS||key==KeyEvent.VK_EQUALS)camera.zoom+=.15;
                    if(key==KeyEvent.VK_SUBTRACT||key==KeyEvent.VK_MINUS)camera.zoom-=.15;
                    if(key==KeyEvent.VK_R){viewX=GameMap.SPAWN_X;viewY=GameMap.SPAWN_Y;world.reset();camera.zoom=1.25;}
                }
                public void keyReleased(KeyEvent e){keys.remove(e.getKeyCode());}
            });
            addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){requestFocusInWindow();}
            });
            addMouseWheelListener(e->{camera.zoom-=e.getPreciseWheelRotation()*.1;});
        }
        boolean held(int...codes){for(int code:codes)if(keys.contains(code))return true;return false;}
        void start(){previous=System.nanoTime();new Timer(16,e->tick()).start();}
        void tick(){
            long now=System.nanoTime();double dt=Math.min(.05,(now-previous)/1_000_000_000.0);previous=now;
            double dx=(held(KeyEvent.VK_D,KeyEvent.VK_RIGHT)?1:0)-(held(KeyEvent.VK_Q,KeyEvent.VK_A,KeyEvent.VK_LEFT)?1:0);
            double dy=(held(KeyEvent.VK_S,KeyEvent.VK_DOWN)?1:0)-(held(KeyEvent.VK_Z,KeyEvent.VK_W,KeyEvent.VK_UP)?1:0);
            double length=Math.hypot(dx,dy);
            if(length>0){
                viewX+=dx/length*420*dt;viewY+=dy/length*420*dt;
            }
            camera.follow(viewX,viewY,getWidth(),getHeight(),world.width,world.height);
            viewX=camera.x;viewY=camera.y;repaint();
        }
        protected void paintComponent(Graphics original){
            super.paintComponent(original);
            Graphics2D g=(Graphics2D)original.create();g.setClip(0,0,getWidth(),getHeight());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.translate(Math.rint(getWidth()/2.0-camera.x*camera.zoom),Math.rint(getHeight()/2.0-camera.y*camera.zoom));g.scale(camera.zoom,camera.zoom);
            world.draw(g,0,()->{},debug);g.dispose();
            drawHud((Graphics2D)original);
        }
        void drawHud(Graphics2D original){
            Graphics2D g=(Graphics2D)original.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0,0,new Color(8,16,12,160),0,85,new Color(8,16,12,0)));g.fillRect(0,0,getWidth(),85);
            g.setFont(new Font("SansSerif",Font.BOLD,14));g.setColor(new Color(220,206,157));g.drawString("LE DEBARQUEMENT D'HUGO",28,32);
            if(miniMap){
                int mx=getWidth()-254,my=24;
                g.setColor(new Color(13,22,17,220));g.fillRoundRect(mx-7,my-7,244,174,8,8);g.drawImage(world.overview(),mx,my,null);
                g.setColor(new Color(220,206,157,150));g.drawRect(mx,my,230,160);
                g.setColor(new Color(255,255,230,160));
                g.drawRect(mx+(int)((camera.x-getWidth()/(2*camera.zoom))/world.width*230),my+(int)((camera.y-getHeight()/(2*camera.zoom))/world.height*160),(int)(getWidth()/camera.zoom/world.width*230),(int)(getHeight()/camera.zoom/world.height*160));
            }
            g.setFont(new Font("SansSerif",Font.PLAIN,12));String controls="ZQSD / Fleches : camera libre     Molette : zoom     M : carte";
            g.setColor(new Color(15,22,17,170));g.fillRoundRect(20,getHeight()-43,g.getFontMetrics().stringWidth(controls)+24,26,7,7);
            g.setColor(new Color(226,226,202));g.drawString(controls,32,getHeight()-25);
            if(debug){g.setColor(Color.WHITE);g.drawString(world.entityCount()+" objets | camera "+(int)camera.x+", "+(int)camera.y,28,60);}
            g.dispose();
        }
    }
}
