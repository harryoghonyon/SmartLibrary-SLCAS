package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Map;

/**
 * Admin tab: add/delete items & users, undo, import/export, reports.
 * Uses CardLayout to switch between admin sub-screens.
 */
public class AdminPanel extends JPanel {
    private final LibraryManager manager;
    private final Runnable onDataChanged;

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JTextArea reportArea = new JTextArea(12, 50);

    // Dynamic fields panel for item type
    private final JPanel dynamicFields = new JPanel(new GridBagLayout());
    private final JTextField titleField = new JTextField(18);
    private final JTextField authorField = new JTextField(18);
    private final JTextField yearField = new JTextField(8);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
    private JTextField extra1Field = new JTextField(14);
    private JTextField extra2Field = new JTextField(14);
    private JLabel extra1Label = new JLabel("ISBN:");
    private JLabel extra2Label = new JLabel("Pages:");

    private final JTextField deleteIdField = new JTextField(12);
    private final JTextField userNameField = new JTextField(16);
    private final JTextField userEmailField = new JTextField(16);

    public AdminPanel(LibraryManager manager, Runnable onDataChanged) {
        this.manager = manager;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel nav = new JPanel();
        JButton addItemBtn = new JButton("Add Item");
        JButton usersBtn = new JButton("Users");
        JButton deleteBtn = new JButton("Delete / Undo");
        JButton reportsBtn = new JButton("Reports");
        JButton filesBtn = new JButton("Save / Load");

        addItemBtn.addActionListener(e -> cards.show(cardPanel, "add"));
        usersBtn.addActionListener(e -> cards.show(cardPanel, "users"));
        deleteBtn.addActionListener(e -> cards.show(cardPanel, "delete"));
        reportsBtn.addActionListener(e -> {
            cards.show(cardPanel, "reports");
            generateReports();
        });
        filesBtn.addActionListener(e -> cards.show(cardPanel, "files"));

        nav.add(addItemBtn);
        nav.add(usersBtn);
        nav.add(deleteBtn);
        nav.add(reportsBtn);
        nav.add(filesBtn);

        cardPanel.add(buildAddItemCard(), "add");
        cardPanel.add(buildUsersCard(), "users");
        cardPanel.add(buildDeleteCard(), "delete");
        cardPanel.add(buildReportsCard(), "reports");
        cardPanel.add(buildFilesCard(), "files");

        add(nav, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        cards.show(cardPanel, "add");
    }

    private JPanel buildAddItemCard() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Add Library Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y;
        form.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        form.add(typeCombo, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        form.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        titleField.setToolTipText("Full title of the item");
        form.add(titleField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        form.add(new JLabel("Author / Publisher:"), gbc);
        gbc.gridx = 1;
        form.add(authorField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        form.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        form.add(yearField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        rebuildDynamicFields("Book");
        form.add(dynamicFields, gbc);

        typeCombo.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            rebuildDynamicFields(type);
            dynamicFields.revalidate();
            dynamicFields.repaint();
        });

        JButton saveBtn = new JButton("Add Item");
        saveBtn.setMnemonic('A');
        saveBtn.addActionListener(e -> addItem());

        JPanel south = new JPanel();
        south.add(saveBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    /** Dynamic components — fields change at runtime based on type. */
    private void rebuildDynamicFields(String type) {
        dynamicFields.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        extra1Field = new JTextField(14);
        extra2Field = new JTextField(14);

        if ("Book".equals(type)) {
            extra1Label = new JLabel("ISBN:");
            extra2Label = new JLabel("Pages:");
            extra1Field.setToolTipText("ISBN number");
            extra2Field.setToolTipText("Page count");
        } else if ("Magazine".equals(type)) {
            extra1Label = new JLabel("Issue #:");
            extra2Label = new JLabel("Frequency:");
            extra1Field.setToolTipText("Issue number");
            extra2Field.setToolTipText("e.g. Monthly, Weekly");
        } else {
            extra1Label = new JLabel("Volume:");
            extra2Label = new JLabel("DOI:");
            extra1Field.setToolTipText("Journal volume");
            extra2Field.setToolTipText("Digital Object Identifier");
        }

        gbc.gridx = 0; gbc.gridy = 0;
        dynamicFields.add(extra1Label, gbc);
        gbc.gridx = 1;
        dynamicFields.add(extra1Field, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        dynamicFields.add(extra2Label, gbc);
        gbc.gridx = 1;
        dynamicFields.add(extra2Field, gbc);
    }

    private void addItem() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String yearText = yearField.getText().trim();
            if (title.isEmpty() || author.isEmpty() || yearText.isEmpty()) {
                throw new IllegalArgumentException("Title, author, and year are required.");
            }
            int year = Integer.parseInt(yearText);
            if (year < 1000 || year > 2100) {
                throw new IllegalArgumentException("Year looks invalid.");
            }
            String type = (String) typeCombo.getSelectedItem();
            LibraryItem created;
            if ("Book".equals(type)) {
                int pages = Integer.parseInt(extra2Field.getText().trim());
                created = manager.addBook(title, author, year, extra1Field.getText().trim(), pages);
            } else if ("Magazine".equals(type)) {
                int issue = Integer.parseInt(extra1Field.getText().trim());
                created = manager.addMagazine(title, author, year, issue, extra2Field.getText().trim());
            } else {
                created = manager.addJournal(title, author, year,
                        extra1Field.getText().trim(), extra2Field.getText().trim());
            }
            JOptionPane.showMessageDialog(this, "Added: " + created.getId() + " — " + created.getTitle(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            titleField.setText("");
            authorField.setText("");
            yearField.setText("");
            extra1Field.setText("");
            extra2Field.setText("");
            notifyChanged();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for year/pages/issue.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildUsersCard() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Register User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        form.add(userNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        form.add(userEmailField, gbc);

        JButton addUserBtn = new JButton("Add User");
        addUserBtn.addActionListener(e -> {
            try {
                String name = userNameField.getText().trim();
                String email = userEmailField.getText().trim();
                if (name.isEmpty() || email.isEmpty() || !email.contains("@")) {
                    throw new IllegalArgumentException("Enter a valid name and email address.");
                }
                UserAccount u = manager.addUser(name, email);
                JOptionPane.showMessageDialog(this, "Created user " + u.getUserId(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                userNameField.setText("");
                userEmailField.setText("");
                notifyChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JTextArea list = new JTextArea(8, 40);
        list.setEditable(false);
        JButton refresh = new JButton("List Users");
        refresh.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (UserAccount u : manager.getDatabase().getAllUsers()) {
                sb.append(u).append(" | borrowed now: ")
                        .append(u.getCurrentlyBorrowed()).append("\n");
            }
            list.setText(sb.toString());
        });

        JPanel south = new JPanel();
        south.add(addUserBtn);
        south.add(refresh);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildDeleteCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Delete Item / Undo Admin Action"));

        JPanel row = new JPanel();
        row.add(new JLabel("Item ID to delete:"));
        row.add(deleteIdField);
        JButton delBtn = new JButton("Delete Item");
        delBtn.setToolTipText("Deletes item and pushes restore action onto undo Stack");
        delBtn.addActionListener(e -> {
            try {
                String id = deleteIdField.getText().trim();
                if (id.isEmpty()) {
                    throw new IllegalArgumentException("Enter an item ID.");
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete item " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                LibraryItem removed = manager.deleteItem(id);
                JOptionPane.showMessageDialog(this, "Deleted: " + removed.getTitle()
                        + "\nUse Undo to restore.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                deleteIdField.setText("");
                notifyChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        row.add(delBtn);

        JButton undoBtn = new JButton("Undo Last Admin Action");
        undoBtn.setMnemonic('U');
        undoBtn.setToolTipText("Pop last action from Stack (Alt+U)");
        undoBtn.addActionListener(e -> {
            String msg = manager.undoLastAdminAction();
            JOptionPane.showMessageDialog(this, msg, "Undo", JOptionPane.INFORMATION_MESSAGE);
            notifyChanged();
        });

        panel.add(row);
        panel.add(Box.createVerticalStrut(12));
        panel.add(undoBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("Undo uses a Stack: last add/delete can be reversed."));
        return panel;
    }

    private JPanel buildReportsCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Reports"));
        reportArea.setEditable(false);
        JButton regen = new JButton("Refresh Reports");
        regen.addActionListener(e -> generateReports());
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        panel.add(regen, BorderLayout.SOUTH);
        return panel;
    }

    private void generateReports() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MOST BORROWED ITEMS ===\n");
        for (LibraryItem item : manager.mostBorrowedItems(10)) {
            sb.append(String.format("  [%d] %s — %s (%s)%n",
                    item.getBorrowCount(), item.getId(), item.getTitle(), item.getType()));
        }

        sb.append("\n=== USERS WITH OVERDUE ITEMS ===\n");
        var overdue = manager.usersWithOverdueItems();
        if (overdue.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (String line : overdue) {
                sb.append("  ").append(line).append("\n");
            }
        }

        sb.append("\n=== CATEGORY DISTRIBUTION (recursive count) ===\n");
        Map<String, Integer> dist = manager.categoryDistribution();
        for (var e : dist.entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }

        sb.append("\n=== FREQUENT ACCESS CACHE ===\n  ");
        String[] cache = manager.getDatabase().getFrequentAccessCache();
        if (cache.length == 0) {
            sb.append("(empty — open items to populate)\n");
        } else {
            sb.append(String.join(", ", cache)).append("\n");
        }

        sb.append("\n=== POLYMORPHIC ITEM TRACE ===\n");
        manager.processAllItems(line -> sb.append("  ").append(line).append("\n"));

        reportArea.setText(sb.toString());
    }

    private JPanel buildFilesCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Persistence"));

        JButton saveBtn = new JButton("Save Data");
        saveBtn.setToolTipText("Save catalogue & users to default data file");
        saveBtn.addActionListener(e -> {
            try {
                manager.save();
                JOptionPane.showMessageDialog(this,
                        "Saved to:\n" + manager.getFileHandler().getDataPath(),
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton loadBtn = new JButton("Load Data");
        loadBtn.addActionListener(e -> {
            try {
                manager.load();
                JOptionPane.showMessageDialog(this, "Data loaded.", "Loaded",
                        JOptionPane.INFORMATION_MESSAGE);
                notifyChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton exportBtn = new JButton("Export via File Chooser…");
        exportBtn.setToolTipText("Choose a destination file for export");
        exportBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export library data");
            chooser.setFileFilter(new FileNameExtensionFilter("Text / Data files", "txt", "dat", "json"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    new utils.FileHandler(file.getAbsolutePath()).save(manager.getDatabase());
                    JOptionPane.showMessageDialog(this, "Exported to " + file.getAbsolutePath(),
                            "Export", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton importBtn = new JButton("Import via File Chooser…");
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Import library data");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    new utils.FileHandler(file.getAbsolutePath()).load(manager.getDatabase());
                    JOptionPane.showMessageDialog(this, "Imported from " + file.getAbsolutePath(),
                            "Import", JOptionPane.INFORMATION_MESSAGE);
                    notifyChanged();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Import Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(saveBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(loadBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(exportBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(importBtn);
        return panel;
    }

    private void notifyChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
