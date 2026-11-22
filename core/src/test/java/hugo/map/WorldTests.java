package hugo.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayDeque;
import java.util.EnumSet;

public final class WorldTests {
    private static void check(boolean ok, String text) {
        if (!ok) throw new AssertionError(text);
    }

    public static void main(String[] args) throws Exception {
        GameMap map = new GameMap();
        check(map.width == 4608 && map.height == 3200, "map dimensions");
        check(map.entityCount() == 660, "all map objects loaded");
        check(map.canMove(GameMap.playerBox(GameMap.SPAWN_X, GameMap.SPAWN_Y)), "spawn is clear");
        check(!map.canMove(GameMap.playerBox(0, 500)), "left edge");
        check(!map.canMove(GameMap.playerBox(map.width, 500)), "right edge");
        check(!map.canMove(GameMap.playerBox(500, 0)), "top edge");
        check(!map.canMove(GameMap.playerBox(500, map.height + 1)), "bottom edge");
        check(!map.canMove(GameMap.playerBox(650, 2400)), "water blocks movement");
        for (int x : GameMap.BRIDGES) {
            Vector2 p = map.move(x, 2300, 0, 250);
            check(Math.abs(p.y - 2550) < .02, "bridge is passable " + x);
        }
        EnumSet<DecorType> used = EnumSet.noneOf(DecorType.class);
        int chests = 0, collisions = 0, bushes = 0;
        float previous = -1;
        for (DecorObject e : map.getEntities()) {
            check(e.y >= previous, "depth order");
            previous = e.y;
            used.add(e.getType());
            if (e.getType().footprint.equals("tree") || e.getType() == DecorType.BUSH)
                check(!map.overlapsRoad(e), "vegetation stays off roads " + e.id);
            if (e.getType() == DecorType.BUSH) bushes++;
            if (e.collision != null) {
                collisions++;
                Rectangle r = e.collision;
                Rectangle roadClearance =
                        new Rectangle(r.x - 24, r.y - 24, r.width + 48, r.height + 48);
                check(
                        !map.areas.get("roads").intersects(roadClearance),
                        "road stays clear " + e.id);
                check(
                        !map.canMove(
                                new Rectangle(r.x + r.width / 2 - 1, r.y + r.height / 2 - 1, 2, 2)),
                        "collision " + e.id);
            }
            if (e.getType() == DecorType.FENCE_LONG) {
                Rectangle r = e.collision;
                Vector2 stop = map.move(e.x, r.y + r.height + 55, 0, -220);
                check(stop.y >= r.y + r.height, "fence stops movement");
                check(
                        map.blocksShot(
                                r.x - 5, r.y + r.height / 2, r.x + r.width + 5, r.y + r.height / 2),
                        "walls stop shots");
            }
            if (e.isChest()) chests++;
        }
        check(bushes == 178, "bush count preserved");
        for (DecorType type : DecorType.values())
            if (type != DecorType.OPEN_CHEST)
                check(used.contains(type), "decor type preserved " + type);
        boolean[] reach = reachable(map);
        int cols = map.width / 16 + 1;
        for (DecorObject e : map.getEntities())
            if (e.isChest()) {
                boolean found = false;
                for (int y = (int) e.y - 64; y <= e.y + 64; y += 8)
                    for (int x = (int) e.x - 64; x <= e.x + 64; x += 8) {
                        int cx = Math.round(x / 16f), cy = Math.round(y / 16f);
                        if (Vector2.dst(cx * 16, cy * 16, e.x, e.y) < 65 && reach[cy * cols + cx])
                            found = true;
                    }
                check(found, "chest is reachable " + e.id);
            }
        check(reach[56 * cols + 41] && reach[56 * cols + 59], "clearing is reachable");
        final int[] rewards = {0};
        map.setChestListener(id -> rewards[0]++);
        DecorObject first = null;
        for (DecorObject e : map.getEntities())
            if (e.isChest()) {
                first = e;
                map.updateTriggers(e.x, e.y + 45);
                map.updateChests(0.5f);
                map.updateTriggers(e.x, e.y + 45);
            }
        check(rewards[0] == chests && chests == 6, "one reward per chest opening");
        map.updateTriggers(first.x, first.y + 45);
        map.updateChests(110f);
        check(first.getType() == DecorType.OPEN_CHEST, "chest stays open");
        map.updateChests(11f);
        check(first.getType() == DecorType.CHEST, "chest closes after two minutes");
        map.updateTriggers(first.x, first.y + 45);
        map.updateChests(0.5f);
        check(rewards[0] == chests, "chest requires leaving its trigger");
        map.updateTriggers(GameMap.SPAWN_X, GameMap.SPAWN_Y);
        map.updateTriggers(first.x, first.y + 45);
        map.updateChests(0.5f);
        check(rewards[0] == chests + 1, "chest can be reused");
        map.reset();
        map.updateTriggers(first.x, first.y + 45);
        map.updateChests(0.5f);
        check(rewards[0] == chests + 2, "chests reset");
        check("MILITARY BASE".equals(map.zoneAt(3100, 800)), "base area preserved");
        check("TRENCHES".equals(map.zoneAt(3900, 1900)), "trench area preserved");
        check("NORTHERN FOREST".equals(map.zoneAt(500, 400)), "forest area preserved");
        MapCamera camera = new MapCamera();
        camera.follow(-100, -100, 1440, 900, map.width, map.height);
        check(
                camera.x - 1440 / (2 * camera.zoom) >= 0 && camera.y - 900 / (2 * camera.zoom) >= 0,
                "camera bounds");
        camera.follow(99999, 99999, 1440, 900, map.width, map.height);
        check(
                camera.x + 1440 / (2 * camera.zoom) <= map.width
                        && camera.y + 900 / (2 * camera.zoom) <= map.height,
                "opposite camera bounds");
        MapArea water = map.areas.get("riverShape");
        check(water.contains(650, 2400) && !water.contains(650, 2000), "river shape");
        System.out.println(
                "OK libGDX : "
                        + map.entityCount()
                        + " objects, "
                        + collisions
                        + " collisions, 6 reachable chests, 2 bridges, clear roads, camera and"
                        + " areas");
    }

    private static boolean[] reachable(GameMap map) {
        int cols = map.width / 16 + 1, rows = map.height / 16 + 1;
        boolean[] seen = new boolean[cols * rows];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int start = Math.round(GameMap.SPAWN_Y / 16) * cols + Math.round(GameMap.SPAWN_X / 16);
        queue.add(start);
        seen[start] = true;
        int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            int key = queue.remove(), x = key % cols, y = key / cols;
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i], ny = y + dy[i];
                if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) continue;
                int n = ny * cols + nx;
                if (!seen[n] && map.canMove(GameMap.playerBox(nx * 16, ny * 16))) {
                    seen[n] = true;
                    queue.add(n);
                }
            }
        }
        return seen;
    }
}
