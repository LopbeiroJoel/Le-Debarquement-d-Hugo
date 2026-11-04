package hugo.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;

public final class ZoneCarte {
    public final List<float[]> polygons=new ArrayList<>();
    private final Rectangle bounds=new Rectangle();
    public ZoneCarte(JsonValue value){
        float left=Float.POSITIVE_INFINITY,top=left,right=Float.NEGATIVE_INFINITY,bottom=right;
        for(JsonValue part=value.child;part!=null;part=part.next){
            float[] points=part.asFloatArray();polygons.add(points);
            for(int i=0;i<points.length;i+=2){left=Math.min(left,points[i]);top=Math.min(top,points[i+1]);right=Math.max(right,points[i]);bottom=Math.max(bottom,points[i+1]);}
        }
        bounds.set(left,top,right-left,bottom-top);
    }
    public boolean contains(float x,float y){
        if(!bounds.contains(x,y))return false;
        boolean inside=false;
        for(float[] p:polygons)for(int i=0,j=p.length-2;i<p.length;j=i,i+=2){
            if((p[i+1]>y)!=(p[j+1]>y)&&x<(p[j]-p[i])*(y-p[i+1])/(p[j+1]-p[i+1])+p[i])inside=!inside;
        }
        return inside;
    }
    public boolean intersects(Rectangle box){
        if(!bounds.overlaps(box))return false;
        if(contains(box.x,box.y)||contains(box.x+box.width,box.y)||contains(box.x,box.y+box.height)||contains(box.x+box.width,box.y+box.height))return true;
        for(float[] p:polygons)for(int i=0,j=p.length-2;i<p.length;j=i,i+=2)
            if(segment(box,p[j],p[j+1],p[i],p[i+1]))return true;
        return false;
    }
    public static boolean segment(Rectangle box,float x1,float y1,float x2,float y2){
        float low=0,high=1,dx=x2-x1,dy=y2-y1;
        float[] p={-dx,dx,-dy,dy},q={x1-box.x,box.x+box.width-x1,y1-box.y,box.y+box.height-y1};
        for(int i=0;i<4;i++){
            if(p[i]==0){if(q[i]<0)return false;}
            else {float t=q[i]/p[i];if(p[i]<0)low=Math.max(low,t);else high=Math.min(high,t);if(low>high)return false;}
        }
        return true;
    }
}
