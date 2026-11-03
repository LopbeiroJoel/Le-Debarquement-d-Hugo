import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Le Debarquement d Hugo");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(1280, 720);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
