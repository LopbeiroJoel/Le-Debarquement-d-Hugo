// Sert à savoir si on est dans une zone ou si on la touche // clauder
// Genre la rivière bloque le perso et on évite de mettre des arbres sur les routes

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

    public boolean contains(float x, float y) {
        if (!bounds.contains(x, y)) {
            return false;
        }

        boolean inside = false;

        for (float[] points : polygons) {
            int precedent = points.length - 2;

            for (int i = 0; i < points.length; i = i + 2) {
                float actuelX = points[i];
                float actuelY = points[i + 1];

                float precedentX = points[precedent];
                float precedentY = points[precedent + 1];

                boolean traverseHauteur =
                    (actuelY > y) != (precedentY > y);

                if (traverseHauteur) {
                    float intersectionX =
                        (precedentX - actuelX)
                            * (y - actuelY)
                            / (precedentY - actuelY)
                            + actuelX;

                    if (x < intersectionX) {
                        inside = !inside;
                    }
                }

                precedent = i;
            }
        }

        return inside;
    }

    public boolean intersects(Rectangle box) {
        if (!bounds.overlaps(box)) {
            return false;
        }

        float gauche = box.x;
        float droite = box.x + box.width;

        float haut = box.y;
        float bas = box.y + box.height;

        if (contains(gauche, haut)) {
            return true;
        }

        if (contains(droite, haut)) {
            return true;
        }

        if (contains(gauche, bas)) {
            return true;
        }

        if (contains(droite, bas)) {
            return true;
        }

        for (float[] points : polygons) {
            int precedent = points.length - 2;

            for (int i = 0; i < points.length; i = i + 2) {
                boolean touche = segment(
                    box,
                    points[precedent],
                    points[precedent + 1],
                    points[i],
                    points[i + 1]
                );

                if (touche) {
                    return true;
                }

                precedent = i;
            }
        }

        return false;
    }

    public static boolean segment(
        Rectangle box,
        float x1,
        float y1,
        float x2,
        float y2
    ) {
        float debut = 0;
        float fin = 1;

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
                float proportion = distance / direction;

                if (direction < 0) {
                    debut = Math.max(debut, proportion);
                } else {
                    fin = Math.min(fin, proportion);
                }

                if (debut > fin) {
                    return false;
                }
            }
        }

        return true;
    }
}
