package world;

public final class Camera {
    public double x,y,zoom=2.0;
    public void follow(double targetX,double targetY,int screenW,int screenH,int mapW,int mapH){
        double minimum=Math.max(1.0,Math.max(screenW/(double)mapW,screenH/(double)mapH));
        zoom=Math.max(minimum,Math.min(4.0,zoom));
        double halfW=screenW/(2.0*zoom),halfH=screenH/(2.0*zoom);
        x=Math.max(halfW,Math.min(mapW-halfW,targetX));
        y=Math.max(halfH,Math.min(mapH-halfH,targetY));
    }
}
