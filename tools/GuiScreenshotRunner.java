import controller.LibraryManager;
import gui.MainWindow;

import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiScreenshotRunner {
    private static final int WIDTH = 1120;
    private static final int HEIGHT = 760;

    public static void main(String[] args) throws Exception {
        Path outputDir = Path.of(args.length > 0 ? args[0] : "gui-screenshots");
        Files.createDirectories(outputDir);

        LibraryManager manager = new LibraryManager(Path.of("data", "library_data.txt")
                .toAbsolutePath().toString());
        try {
            manager.load();
        } catch (Exception ignored) {
            // Seed data is enough for screenshots when no saved file exists.
        }
        manager.loadSeedDataIfEmpty();

        final MainWindow[] windowRef = new MainWindow[1];
        SwingUtilities.invokeAndWait(() -> {
            MainWindow window = new MainWindow(manager);
            window.setSize(new Dimension(WIDTH, HEIGHT));
            window.setLocationRelativeTo(null);
            window.setAlwaysOnTop(true);
            window.setVisible(true);
            window.toFront();
            window.requestFocus();
            windowRef[0] = window;
        });

        Robot robot = new Robot();
        robot.setAutoDelay(250);
        robot.delay(1000);

        MainWindow window = windowRef[0];
        JTabbedPane tabs = findTabbedPane(window);
        if (tabs == null) {
            throw new IllegalStateException("Could not find application tabs.");
        }

        for (int i = 0; i < tabs.getTabCount(); i++) {
            final int tabIndex = i;
            SwingUtilities.invokeAndWait(() -> {
                tabs.setSelectedIndex(tabIndex);
                window.toFront();
                window.repaint();
            });
            robot.delay(500);
            String title = tabs.getTitleAt(i).toLowerCase()
                    .replace(" / ", "-")
                    .replace(" & ", "-")
                    .replace(" ", "-");
            writeWindow(outputDir.resolve(String.format("%d-%s.png", i + 1, title)), window, robot);
        }

        SwingUtilities.invokeAndWait(() -> {
            window.setAlwaysOnTop(false);
            window.dispose();
        });
    }

    private static void writeWindow(Path path, MainWindow window, Robot robot) throws Exception {
        Rectangle bounds = window.getBounds();
        if (bounds.width <= 0 || bounds.height <= 0) {
            throw new IllegalStateException("Window bounds are not capturable: " + bounds);
        }
        ImageIO.write(robot.createScreenCapture(bounds), "png", path.toFile());
    }

    private static JTabbedPane findTabbedPane(Component component) {
        if (component instanceof JTabbedPane) {
            return (JTabbedPane) component;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                JTabbedPane found = findTabbedPane(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
