// collisions et coffres

package hugo.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CarteJeu {

    public static final float SPAWN_X = 2200;
    public static final float SPAWN_Y = 1500;

    public static final float WATER_TOP = 2330;
    public static final float WATER_BOTTOM = 2510;

    public static final int[] BRIDGES = {1250, 3420};

    private static final int CELL_SIZE = 128;
    private static final int GRID_COLUMNS = 64;

    public final int width;
    public final int height;

    private final List<ObjetDecor> entities = new ArrayList<>();
    private final List<ObjetDecor> chests = new ArrayList<>();

    private final Map<Integer, List<ObjetDecor>> cells = new HashMap<>();

    public final List<Rectangle> trenchWalls = new ArrayList<>();

    public final Map<String, ZoneCarte> areas = new HashMap<>();

    private Consumer<String> chestListener = id -> {};

    public CarteJeu() throws IOException {
        JsonValue data = readMapFile();

        width = data.getInt("width");
        height = data.getInt("height");

        loadAreas(data);
        loadObjects(data);
        addTrenchWalls();

        Rectangle spawnPosition = playerBox(SPAWN_X, SPAWN_Y);

        if (!canMove(spawnPosition)) {
            throw new IOException("Depart bloque");
        }
    }

    private JsonValue readMapFile() throws IOException {
        try (
            InputStream file = CarteJeu.class.getResourceAsStream(
                "/maps/carte.json"
            )
        ) {
            if (file == null) {
                throw new IOException("Map introuvable");
            }

            JsonReader reader = new JsonReader();

            return reader.parse(file);
        }
    }

    private void loadAreas(JsonValue data) {
        JsonValue zone = data.get("areas").child;

        while (zone != null) {
            ZoneCarte newArea = new ZoneCarte(zone);

            areas.put(zone.name, newArea);

            zone = zone.next;
        }
    }

    private void loadObjects(JsonValue data) {
        JsonValue object = data.get("entities").child;

        while (object != null) {
            String typeName = object.getString("type");

            TypeDecor type = TypeDecor.valueOf(typeName);

            float x = object.getFloat("x");
            float y = object.getFloat("y");

            add(type, x, y);

            object = object.next;
        }
    }

    private void addTrenchWalls() {
        trenchWalls.add(new Rectangle(3330, 1620, 870, 18));
        trenchWalls.add(new Rectangle(3330, 1683, 830, 18));

        trenchWalls.add(new Rectangle(3510, 1810, 650, 18));
        trenchWalls.add(new Rectangle(3570, 1873, 590, 18));

        trenchWalls.add(new Rectangle(3570, 2010, 750, 18));
        trenchWalls.add(new Rectangle(3510, 2073, 810, 18));
    }

    public void add(TypeDecor type, float x, float y) {
        String id = type.name() + "_" + entities.size();

        ObjetDecor object = new ObjetDecor(id, type, x, y);

        entities.add(object);
        if (object.isChest()) {
            chests.add(object);
        }

        entities.sort(
            Comparator.comparingDouble(entity -> entity.y)
        );

        if (object.collision != null) {
            indexCollision(object);
        }
    }

    private void indexCollision(ObjetDecor object) {
        Rectangle collision = object.collision;

        int firstColumn = (int) collision.x / CELL_SIZE;

        int lastColumn =
            (int) (collision.x + collision.width) / CELL_SIZE;

        int firstRow = (int) collision.y / CELL_SIZE;

        int lastRow =
            (int) (collision.y + collision.height) / CELL_SIZE;

        for (int row = firstRow; row <= lastRow; row++) {
            for (
                int column = firstColumn;
                column <= lastColumn;
                column++
            ) {
                int cell = row * GRID_COLUMNS + column;

                cells.computeIfAbsent(cell, key -> new ArrayList<>()).add(object);
            }
        }
    }

    public int entityCount() {
        return entities.size();
    }

    public List<ObjetDecor> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public boolean overlapsRoad(ObjetDecor object) {
        TypeDecor type = object.getType();

        float left = object.x - type.width / 2f;
        float top = object.y - type.height;

        Rectangle objectBounds = new Rectangle(
            left,
            top,
            type.width,
            type.height
        );

        ZoneCarte roads = areas.get("roads");

        return roads.intersects(objectBounds);
    }

    public static Rectangle playerBox(float x, float feetY) {
        return new Rectangle(x - 11, feetY - 17, 22, 17);
    }

    // collisions de la map
    public boolean canMove(Rectangle position) {
        if (isOutsideMap(position)) {
            return false;
        }

        ZoneCarte river = areas.get("riverShape");

        if (!isOnBridge(position) && river.intersects(position)) {
            return false;
        }

        for (Rectangle wall : trenchWalls) {
            if (wall.overlaps(position)) {
                return false;
            }
        }

        return !hitsDecor(position);
    }

    private boolean isOutsideMap(Rectangle position) {
        return position.x < 0 || position.y < 0
            || position.x + position.width > width || position.y + position.height > height;
    }

    private boolean isOnBridge(Rectangle position) {
        float playerLeft = position.x;
        float playerRight = position.x + position.width;

        for (int bridgeCenter : BRIDGES) {
            float bridgeLeft = bridgeCenter - 60;
            float bridgeRight = bridgeCenter + 60;

            if (
                playerLeft >= bridgeLeft &&
                playerRight <= bridgeRight
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean hitsDecor(Rectangle position) {
        int firstColumn = (int) position.x / CELL_SIZE;

        int lastColumn =
            (int) (position.x + position.width) / CELL_SIZE;

        int firstRow = (int) position.y / CELL_SIZE;

        int lastRow =
            (int) (position.y + position.height) / CELL_SIZE;

        for (int row = firstRow; row <= lastRow; row++) {
            for (
                int column = firstColumn;
                column <= lastColumn;
                column++
            ) {
                int cell = row * GRID_COLUMNS + column;

                List<ObjetDecor> cellObjects = cells.get(cell);

                if (cellObjects != null) {
                    for (ObjetDecor object : cellObjects) {
                        if (object.collision.overlaps(position)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // avance par petits pas
    public Vector2 move(float x, float y, float dx, float dy) {
        float distance = Math.max(Math.abs(dx), Math.abs(dy));
        int steps = Math.max(1, (int) Math.ceil(distance / 4));

        float stepX = dx / steps;
        float stepY = dy / steps;

        for (int step = 0; step < steps; step++) {
            Rectangle nextX = playerBox(x + stepX, y);

            if (canMove(nextX)) {
                x += stepX;
            }

            Rectangle nextY = playerBox(x, y + stepY);

            if (canMove(nextY)) {
                y += stepY;
            }
        }

        return new Vector2(x, y);
    }

    // murs bloquent les tirs
    public boolean blocksShot(
        float startX,
        float startY,
        float endX,
        float endY
    ) {
        for (Rectangle wall : trenchWalls) {
            if (ZoneCarte.segment(wall, startX, startY, endX, endY)) {
                return true;
            }
        }

        for (ObjetDecor object : entities) {
            if (object.collision != null && ZoneCarte.segment(object.collision, startX, startY, endX, endY)) {
                return true;
            }
        }

        return false;
    }

    public void setChestListener(Consumer<String> action) {
        chestListener = action == null ? id -> {} : action;
    }

    // hugo approche du coffre
    public void updateTriggers(float x, float y) {
        for (ObjetDecor chest : chests) {
            chest.checkChestTrigger(Vector2.dst2(x, y, chest.x, chest.y) < 68f * 68f);
        }
    }

    public void updateChests(float delta) {
        for (ObjetDecor chest : chests) {
            if (chest.updateChest(delta)) {
                chestListener.accept(chest.id);
            }
        }
    }

    public void reset() {
        for (ObjetDecor chest : chests) {
            chest.reset();
        }
    }

    public String zoneAt(float x, float y) {
        if (y > WATER_TOP - 80 && y < WATER_BOTTOM + 100) {
            return "RIVIERE";
        }

        if (areas.get("base").contains(x, y)) {
            return "BASE MILITAIRE";
        }

        if (areas.get("trenches").contains(x, y)) {
            return "LES TRANCHEES";
        }

        if (areas.get("village").contains(x, y)) {
            return "VILLAGE EN RUINE";
        }

        if (areas.get("forest").contains(x, y)) {
            return "FORET DU NORD";
        }

        if (y > WATER_BOTTOM) {
            return "LE SUD DE LA MAP";
        }

        return "LE BOCAGE";
    }
}
