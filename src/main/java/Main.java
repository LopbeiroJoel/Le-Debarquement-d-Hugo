import world.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public final class Main {
    public static void main(String[] args)throws Exception{
        final GameMap world=new GameMap();
        SwingUtilities.invokeLater(()->{
            GamePanel panel=new GamePanel(world);
            JFrame window=new JFrame("Le Debarquement d'Hugo - Map en entites");
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
        final GameMap world;final Camera camera=new Camera();final Set<Integer> keys=new HashSet<>();
        double x=GameMap.SPAWN_X,y=GameMap.SPAWN_Y;long previous;
        boolean debug=false,paused=false;String message="Approche une caisse HUGO pour tester son ouverture.";
        GamePanel(GameMap world){
            this.world=world;setPreferredSize(new Dimension(1280,720));setFocusable(true);
            world.setChestListener(id->{message="Coffre ouvert : "+id+" - bonus a relier au code de Thomas.";System.out.println(message);});
            addKeyListener(new KeyAdapter(){
                 public void keyPressed(KeyEvent e){int k=e.getKeyCode();if(!keys.add(k))return;
                    if(k==KeyEvent.VK_F1)debug=!debug;
                    if(k==KeyEvent.VK_P||k==KeyEvent.VK_ESCAPE)paused=!paused;
                    if(k==KeyEvent.VK_ADD||k==KeyEvent.VK_PLUS||k==KeyEvent.VK_EQUALS)camera.zoom+=.25;
                    if(k==KeyEvent.VK_SUBTRACT||k==KeyEvent.VK_MINUS)camera.zoom-=.25;
                    if(k==KeyEvent.VK_R){x=GameMap.SPAWN_X;y=GameMap.SPAWN_Y;world.reset();camera.zoom=2;message="Test remis a zero.";}
                }
                 public void keyReleased(KeyEvent e){keys.remove(e.getKeyCode());}
            });
        }
        boolean held(int...codes){for(int c:codes)if(keys.contains(c))return true;return false;}
        void start(){previous=System.nanoTime();new Timer(16,e->tick()).start();}
        void tick(){
            long now=System.nanoTime();double dt=Math.min(.05,(now-previous)/1_000_000_000.0);previous=now;
            if(!paused){
                double dx=(held(KeyEvent.VK_D,KeyEvent.VK_RIGHT)?1:0)-(held(KeyEvent.VK_Q,KeyEvent.VK_A,KeyEvent.VK_LEFT)?1:0);
                double dy=(held(KeyEvent.VK_S,KeyEvent.VK_DOWN)?1:0)-(held(KeyEvent.VK_Z,KeyEvent.VK_W,KeyEvent.VK_UP)?1:0);
                double len=Math.hypot(dx,dy);
                if(len>0){Point2D.Double p=world.move(x,y,dx/len*29*dt,dy/len*29*dt);x=p.x;y=p.y;}
                world.updateTriggers(x,y);
            }
            camera.follow(x,y,getWidth(),getHeight(),world.width,world.height);repaint();
        }
         protected void paintComponent(Graphics original){
            super.paintComponent(original);
            Graphics2D g=(Graphics2D)original.create();
            g.setColor(new Color(22,29,21));g.fillRect(0,0,getWidth(),getHeight());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.translate(getWidth()/2.0,getHeight()/2.0);g.scale(camera.zoom,camera.zoom);g.translate(-camera.x,-camera.y);
            world.draw(g,y,()->{
                g.setColor(new Color(234,226,177));g.fillRect((int)x-6,(int)y-23,12,23);
                g.setColor(new Color(33,45,33));g.drawRect((int)x-6,(int)y-23,12,23);
                if(debug){g.setColor(Color.CYAN);g.draw(GameMap.playerBox(x,y));}
            },debug);g.dispose();
            Graphics2D hud=(Graphics2D)original.create();
            hud.setColor(new Color(14,23,17,230));hud.fillRect(12,12,Math.min(1000,getWidth()-24),86);
            hud.setColor(Color.WHITE);
            hud.drawString("ZQSD / WASD / fleches : rectangle test   |   F1 : collisions   |   +/- : zoom   |   P : pause   |   R : reset",24,33);
            hud.drawString(String.format("%d entites separees   |   Zoom x%.2f   |   Position %.0f, %.0f%s",world.entityCount(),camera.zoom,x,y,paused?"   PAUSE (P pour reprendre)":""),24,55);
            hud.drawString(message,24,78);hud.dispose();
        }
    }
}
