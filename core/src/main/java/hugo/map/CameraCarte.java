package hugo.map;

public final class CameraCarte {
    public double x;
    public double y;
    public double zoom = 1.25;

    // caméra suit hugo
    public void follow(double targetX, double targetY, int screenWidth, int screenHeight,
                       int mapWidth, int mapHeight) {
        double minZoomX = screenWidth / (double) mapWidth;
        double minZoomY = screenHeight / (double) mapHeight;
        double minZoom = Math.max(0.85, Math.max(minZoomX, minZoomY));
        zoom = Math.max(minZoom, Math.min(1.75, zoom));

        double halfWidth = screenWidth / (2.0 * zoom);
        double halfHeight = screenHeight / (2.0 * zoom);
        x = Math.max(halfWidth, Math.min(mapWidth - halfWidth, targetX));
        y = Math.max(halfHeight, Math.min(mapHeight - halfHeight, targetY));
    }
}
