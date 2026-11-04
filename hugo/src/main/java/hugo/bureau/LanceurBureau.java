package hugo.bureau;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import hugo.JeuHugo;
import java.util.Arrays;
import org.lwjgl.glfw.GLFW;

public class LanceurBureau {
    public static void main(String[] args) {
        if(AideDemarrage.startNewJvmIfRequired())return;
        if(Arrays.asList(args).contains("--smoke-test"))System.setProperty("hugo.smokeTest","true");
        if(System.getenv("WSL_DISTRO_NAME")!=null&&System.getenv("DISPLAY")!=null)
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM,GLFW.GLFW_PLATFORM_X11);
        new Lwjgl3Application(new JeuHugo(),getDefaultConfiguration());
    }
    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration=new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("LeDebarquementDHugo");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate+1);
        configuration.setWindowedMode(1440,900);
        configuration.setWindowIcon("libgdx128.png","libgdx64.png","libgdx32.png","libgdx16.png");
        return configuration;
    }
}
