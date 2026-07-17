import controller.LibraryManager;
import gui.MainWindow;

import java.nio.file.Path;

/**
 * Entry point for the Smart Library Circulation & Automation System (COS 202).
 */
public class Main {
    public static void main(String[] args) {
        String dataPath = Path.of("data", "library_data.txt").toAbsolutePath().toString();
        if (args.length > 0) {
            dataPath = args[0];
        }

        LibraryManager manager = new LibraryManager(dataPath);
        try {
            manager.load();
        } catch (Exception ex) {
            System.err.println("Could not load existing data: " + ex.getMessage());
        }
        manager.loadSeedDataIfEmpty();

        MainWindow.launch(manager);
    }
}
