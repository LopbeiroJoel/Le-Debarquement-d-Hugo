import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import game.MapGame;
import org.lwjgl.glfw.GLFW;
import java.util.Arrays;

public final class Main {
    public static void main(String[] args){
        if(System.getenv("WSL_DISTRO_NAME")!=null&&System.getenv("DISPLAY")!=null)GLFW.glfwInitHint(GLFW.GLFW_PLATFORM,GLFW.GLFW_PLATFORM_X11);
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();
        config.setTitle("Le Debarquement d'Hugo - libGDX");
        config.setWindowedMode(1440,900);config.setResizable(true);config.useVsync(true);config.setForegroundFPS(60);
        config.disableAudio(true);
        new Lwjgl3Application(new MapGame(Arrays.asList(args).contains("--smoke-test")),config);
    }
}
