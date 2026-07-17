package gui;

import controller.LibraryManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window with tabbed panels, menu bar, status bar, and overdue timer.
 */
public class MainWindow extends JFrame {
    private final LibraryManager manager;
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel clockLabel = new JLabel();

    private ViewItemsPanel viewPanel;
    private BorrowPanel borrowPanel;
    private AdminPanel adminPanel;
    private SearchSortPanel searchSortPanel;

    public MainWindow(LibraryManager manager) {
        super("Smart Library Circulation & Automation System (SLCAS)");
        this.manager = manager;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        buildMenuBar();
        buildContent();
        buildStatusBar();
        startOverdueTimer();

        refreshAll();
        setStatus("Loaded " + manager.getDatabase().getItems().size() + " items, "
                + manager.getDatabase().getUsers().size() + " users.");
    }

    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.setToolTipText("Save library data");
        saveItem.addActionListener(e -> {
            try {
                manager.save();
                setStatus("Data saved.");
                JOptionPane.showMessageDialog(this, "Saved successfully.", "Save",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadItem.addActionListener(e -> {
            try {
                manager.load();
                refreshAll();
                setStatus("Data loaded.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        JMenuItem undoItem = new JMenuItem("Undo Admin Action");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
            String msg = manager.undoLastAdminAction();
            JOptionPane.showMessageDialog(this, msg, "Undo", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        });
        editMenu.add(undoItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem about = new JMenuItem("About SLCAS");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                """
                        Smart Library Circulation & Automation System
                        COS 202 Project — MIVA Open University

                        Features: OOP hierarchy, ArrayList / Queue / Stack / Array cache,
                        linear & binary & recursive search, selection/insertion/merge/quick sort,
                        event-driven Swing GUI, file persistence, overdue reminders.""",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(about);

        bar.add(fileMenu);
        bar.add(editMenu);
        bar.add(helpMenu);
        setJMenuBar(bar);
    }

    private void buildContent() {
        JPanel root = new JPanel(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 55, 95));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel brand = new JLabel("SLCAS — Smart Library Circulation & Automation");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Serif", Font.BOLD, 20));
        header.add(brand, BorderLayout.WEST);

        Runnable onChange = this::refreshAll;

        viewPanel = new ViewItemsPanel(manager, () -> setStatus("Catalogue refreshed."));
        borrowPanel = new BorrowPanel(manager, onChange);
        adminPanel = new AdminPanel(manager, onChange);
        searchSortPanel = new SearchSortPanel(manager);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("View Items", viewPanel);
        tabs.addTab("Borrow / Return", borrowPanel);
        tabs.addTab("Admin", adminPanel);
        tabs.addTab("Search & Sort", searchSortPanel);
        tabs.setToolTipTextAt(0, "Browse the full catalogue");
        tabs.setToolTipTextAt(1, "Borrow, return, and manage reservations");
        tabs.setToolTipTextAt(2, "Add items/users, undo, reports, save/load");
        tabs.setToolTipTextAt(3, "Search and sort with selectable algorithms");

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusLabel.setToolTipText("Application status messages appear here");
        clockLabel.setHorizontalAlignment(JLabel.RIGHT);
        status.add(statusLabel, BorderLayout.WEST);
        status.add(clockLabel, BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);

        // keep clock updated (also acts as a lightweight timer)
        Timer clock = new Timer(1000, e -> clockLabel.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        clock.start();
    }

    /** Timer-triggered overdue reminders (advanced GUI technique). */
    private void startOverdueTimer() {
        Timer overdueTimer = new Timer(60_000, e -> checkOverdueReminders(false));
        overdueTimer.setInitialDelay(8_000);
        overdueTimer.start();

        // Also run once shortly after startup
        Timer firstCheck = new Timer(3_000, e -> checkOverdueReminders(true));
        firstCheck.setRepeats(false);
        firstCheck.start();
    }

    private void checkOverdueReminders(boolean silentIfNone) {
        var overdue = manager.usersWithOverdueItems();
        if (overdue.isEmpty()) {
            if (!silentIfNone) {
                setStatus("Overdue check: none found.");
            }
            return;
        }
        setStatus("Overdue reminder: " + overdue.size() + " item(s) past due.");
        StringBuilder sb = new StringBuilder("Overdue items detected:\n\n");
        for (String line : overdue) {
            sb.append("• ").append(line).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Overdue Reminder",
                JOptionPane.WARNING_MESSAGE);
    }

    private void refreshAll() {
        viewPanel.refresh();
        borrowPanel.refreshCombos();
        searchSortPanel.refreshFromDatabase();
        setStatus("Items: " + manager.getDatabase().getItems().size()
                + " | Users: " + manager.getDatabase().getUsers().size()
                + " | Undo available: " + manager.canUndo());
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    public static void launch(LibraryManager manager) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            MainWindow window = new MainWindow(manager);
            window.setVisible(true);
        });
    }
}
