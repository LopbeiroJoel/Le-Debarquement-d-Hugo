// zones de la map

package hugo.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public final class ZoneCarte {

    public final List<float[]> polygons = new ArrayList<>();

    private final Rectangle bounds = new Rectangle();

    public ZoneCarte(JsonValue value) {
        float left = Float.POSITIVE_INFINITY;
        float top = Float.POSITIVE_INFINITY;

        float right = Float.NEGATIVE_INFINITY;
        float bottom = Float.NEGATIVE_INFINITY;

        for (
            JsonValue part = value.child;
            part != null;
            part = part.next
        ) {
            float[] points = part.asFloatArray();

            polygons.add(points);

            for (int i = 0; i < points.length; i = i + 2) {
                left = Math.min(left, points[i]);
                top = Math.min(top, points[i + 1]);

                right = Math.max(right, points[i]);
                bottom = Math.max(bottom, points[i + 1]);
            }
        }

        bounds.set(
            left,
            top,
            right - left,
            bottom - top
        );
    }

    // point dans la zone
    public boolean contains(float x, float y) {
        if (!bounds.contains(x, y)) {
            return false;
        }

        boolean inside = false;

        for (float[] points : polygons) {
            int previous = points.length - 2;

            for (int i = 0; i < points.length; i = i + 2) {
                float currentX = points[i];
                float currentY = points[i + 1];

                float previousX = points[previous];
                float previousY = points[previous + 1];

                boolean crossesHeight =
                    (currentY > y) != (previousY > y);

                if (crossesHeight) {
                    float intersectionX =
                        (previousX - currentX)
                            * (y - currentY)
                            / (previousY - currentY)
                            + currentX;

                    if (x < intersectionX) {
                        inside = !inside;
                    }
                }

                previous = i;
            }
        }

        return inside;
    }

    // boîte touche la zone
    public boolean intersects(Rectangle box) {
        if (!bounds.overlaps(box)) {
            return false;
        }

        float left = box.x;
        float right = box.x + box.width;

        float top = box.y;
        float bottom = box.y + box.height;

        if (contains(left, top) || contains(right, top)
                || contains(left, bottom) || contains(right, bottom)) {
            return true;
        }

        for (float[] points : polygons) {
            int previous = points.length - 2;

            for (int i = 0; i < points.length; i = i + 2) {
                if (segment(box, points[previous], points[previous + 1], points[i], points[i + 1])) {
                    return true;
                }

                previous = i;
            }
        }

        return false;
    }

    // ligne traverse la boîte
    public static boolean segment(
        Rectangle box,
        float x1,
        float y1,
        float x2,
        float y2
    ) {
        float start = 0;
        float end = 1;

        float directionX = x2 - x1;
        float directionY = y2 - y1;

        float[] directions = {
            -directionX,
            directionX,
            -directionY,
            directionY
        };

        float[] distances = {
            x1 - box.x,
            box.x + box.width - x1,
            y1 - box.y,
            box.y + box.height - y1
        };

        for (int i = 0; i < 4; i++) {
            float direction = directions[i];
            float distance = distances[i];

            if (direction == 0) {
                if (distance < 0) {
                    return false;
                }
            } else {
                float fraction = distance / direction;

                if (direction < 0) {
                    start = Math.max(start, fraction);
                } else {
                    end = Math.min(end, fraction);
                }

                if (start > end) {
                    return false;
                }
            }
        }

        return true;
    }
}
