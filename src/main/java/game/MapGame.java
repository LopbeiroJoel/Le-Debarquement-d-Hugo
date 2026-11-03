package game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import world.Camera;
import world.GameMap;
import world.MapRenderer;

public final class MapGame extends ApplicationAdapter {
    private final boolean smoke;
    private GameMap world;
    private MapRenderer renderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture white;
    private final Camera camera=new Camera();
    private final OrthographicCamera scene=new OrthographicCamera();
    private final OrthographicCamera hud=new OrthographicCamera();
    private double viewX=GameMap.SPAWN_X,viewY=GameMap.SPAWN_Y;
    private boolean debug,miniMap=true;
    private int frames;

    public MapGame(boolean smoke){this.smoke=smoke;}
    public void create(){
        try{world=new GameMap();}catch(Exception e){throw new IllegalStateException("Chargement de la map",e);}
        batch=new SpriteBatch();renderer=new MapRenderer(world);font=new BitmapFont();
        Pixmap pixel=new Pixmap(1,1,Pixmap.Format.RGBA8888);pixel.setColor(Color.WHITE);pixel.fill();white=new Texture(pixel);pixel.dispose();
        Gdx.input.setInputProcessor(new InputAdapter(){
            public boolean scrolled(float amountX,float amountY){camera.zoom-=amountY*.1;return true;}
        });
    }
    private boolean held(int...keys){for(int key:keys)if(Gdx.input.isKeyPressed(key))return true;return false;}
    public void render(){
        if(smoke&&frames==30){viewX=3100;viewY=850;}
        if(smoke&&frames==60){viewX=3900;viewY=1850;}
        double dt=Math.min(.05,Gdx.graphics.getDeltaTime());
        double dx=(held(Input.Keys.D,Input.Keys.RIGHT)?1:0)-(held(Input.Keys.Q,Input.Keys.A,Input.Keys.LEFT)?1:0);
        double dy=(held(Input.Keys.S,Input.Keys.DOWN)?1:0)-(held(Input.Keys.Z,Input.Keys.W,Input.Keys.UP)?1:0);
        double length=Math.hypot(dx,dy);
        if(length>0){viewX+=dx/length*420*dt;viewY+=dy/length*420*dt;}
        if(Gdx.input.isKeyJustPressed(Input.Keys.F1))debug=!debug;
        if(Gdx.input.isKeyJustPressed(Input.Keys.M))miniMap=!miniMap;
        if(Gdx.input.isKeyJustPressed(Input.Keys.PLUS)||Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)||Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ADD))camera.zoom+=.15;
        if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS)||Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_SUBTRACT))camera.zoom-=.15;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){viewX=GameMap.SPAWN_X;viewY=GameMap.SPAWN_Y;camera.zoom=1.25;world.reset();}
        camera.follow(viewX,viewY,Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),world.width,world.height);
        viewX=camera.x;viewY=camera.y;
        scene.setToOrtho(true,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());scene.zoom=(float)(1/camera.zoom);
        scene.position.set((float)camera.x,(float)camera.y,0);scene.update();
        ScreenUtils.clear(.04f,.07f,.05f,1);
        renderer.draw(batch,scene,0,()->{});if(debug)renderer.drawCollisions(scene);
        drawHud();
        if(smoke&&(++frames==30||frames==60||frames==90)){
            Pixmap screenshot=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local(frames==30?"build/libgdx-smoke.png":frames==60?"build/libgdx-base.png":"build/libgdx-tranchees.png"),screenshot,-1,true);screenshot.dispose();
            Gdx.app.log("Verification","libGDX : "+world.entityCount()+" objets, rendu OpenGL, "+frames+" images sans erreur");if(frames==90)Gdx.app.exit();
        }
    }
    private void drawHud(){
        int width=Gdx.graphics.getWidth(),height=Gdx.graphics.getHeight();hud.setToOrtho(false,width,height);
        batch.setProjectionMatrix(hud.combined);batch.begin();
        batch.setColor(.04f,.07f,.05f,.72f);batch.draw(white,16, height-48,254,30);batch.setColor(Color.WHITE);
        font.setColor(.88f,.84f,.67f,1);font.draw(batch,"LE DEBARQUEMENT D'HUGO",28,height-28);
        if(miniMap){
            float mx=width-254,my=height-184;
            batch.setColor(.04f,.07f,.05f,.85f);batch.draw(white,mx-7,my-7,244,174);batch.setColor(Color.WHITE);batch.draw(renderer.overview,mx,my,230,160);
            float left=mx+(float)((camera.x-width/(2*camera.zoom))/world.width*230);
            float bottom=my+160-(float)((camera.y+height/(2*camera.zoom))/world.height*160);
            float w=(float)(width/camera.zoom/world.width*230),h=(float)(height/camera.zoom/world.height*160);
            batch.setColor(1,1,.85f,.8f);batch.draw(white,left,bottom,w,1);batch.draw(white,left,bottom+h,w,1);batch.draw(white,left,bottom,1,h);batch.draw(white,left+w,bottom,1,h);
        }
        batch.setColor(.04f,.07f,.05f,.72f);batch.draw(white,20,17,478,27);batch.setColor(Color.WHITE);
        font.setColor(.9f,.9f,.8f,1);font.draw(batch,"ZQSD / Fleches : camera libre    Molette : zoom    M : carte",30,35);
        if(debug)font.draw(batch,world.entityCount()+" objets | "+(int)camera.x+", "+(int)camera.y+" | "+Gdx.graphics.getFramesPerSecond()+" FPS",28,height-70);
        batch.setColor(Color.WHITE);batch.end();
    }
    public void dispose(){renderer.dispose();batch.dispose();font.dispose();white.dispose();}
}
