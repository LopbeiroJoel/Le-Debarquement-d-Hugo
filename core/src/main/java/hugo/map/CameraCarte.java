// Ca sert juste à seulement voir la map sans rien voir autour

package hugo.map;

public final class CameraCarte {
    public double x,y,zoom=1.25;
    public void follow(double targetX,double targetY,int screenW,int screenH,int mapW,int mapH){
        double minimum=Math.max(.85,Math.max(screenW/(double)mapW,screenH/(double)mapH));
        zoom=Math.max(minimum,Math.min(1.75,zoom));
        double halfW=screenW/(2.0*zoom),halfH=screenH/(2.0*zoom);
        x=Math.max(halfW,Math.min(mapW-halfW,targetX));
        y=Math.max(halfH,Math.min(mapH-halfH,targetY));
    }
}
