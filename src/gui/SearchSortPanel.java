package gui;

import controller.LibraryManager;
import controller.SearchEngine;
import controller.SortEngine;
import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Search & Sort tab with algorithm dropdowns.
 */
public class SearchSortPanel extends JPanel {
    private final LibraryManager manager;
    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);

    private final JTextField queryField = new JTextField(18);
    private final JComboBox<SearchEngine.Field> fieldCombo =
            new JComboBox<>(SearchEngine.Field.values());
    private final JComboBox<SearchEngine.Mode> searchAlgoCombo =
            new JComboBox<>(SearchEngine.Mode.values());
    private final JComboBox<SortEngine.Algorithm> sortAlgoCombo =
            new JComboBox<>(SortEngine.Algorithm.values());
    private final JComboBox<SortEngine.SortKey> sortKeyCombo =
            new JComboBox<>(SortEngine.SortKey.values());
    private final JCheckBox ascendingBox = new JCheckBox("Ascending", true);
    private final JLabel infoLabel = new JLabel(" ");

    private List<LibraryItem> workingList = new ArrayList<>();

    public SearchSortPanel(LibraryManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBorder(BorderFactory.createTitledBorder("Search & Sort Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controls.add(new JLabel("Query:"), gbc);
        gbc.gridx = 1;
        queryField.setToolTipText("Search text — updates as you type when using Linear mode");
        controls.add(queryField, gbc);

        gbc.gridx = 2;
        controls.add(new JLabel("Field:"), gbc);
        gbc.gridx = 3;
        controls.add(fieldCombo, gbc);

        gbc.gridx = 4;
        controls.add(new JLabel("Search algo:"), gbc);
        gbc.gridx = 5;
        searchAlgoCombo.setToolTipText("Linear, Binary (best on sorted data), or Recursive");
        controls.add(searchAlgoCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controls.add(new JLabel("Sort algo:"), gbc);
        gbc.gridx = 1;
        sortAlgoCombo.setToolTipText("Selection, Insertion, Merge, or Quick Sort");
        controls.add(sortAlgoCombo, gbc);

        gbc.gridx = 2;
        controls.add(new JLabel("Sort by:"), gbc);
        gbc.gridx = 3;
        controls.add(sortKeyCombo, gbc);

        gbc.gridx = 4;
        controls.add(ascendingBox, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Search");
        searchBtn.setMnemonic('S');
        JButton sortBtn = new JButton("Sort");
        sortBtn.setMnemonic('O');
        JButton resetBtn = new JButton("Reset List");
        searchBtn.addActionListener(e -> runSearch());
        sortBtn.addActionListener(e -> runSort());
        resetBtn.addActionListener(e -> resetList());
        btnRow.add(searchBtn);
        btnRow.add(sortBtn);
        btnRow.add(resetBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6;
        controls.add(btnRow, gbc);

        // Live text-field updates for linear search
        queryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { liveSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { liveSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { liveSearch(); }
        });

        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new ViewItemsPanel.StatusCellRenderer());

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        resetList();
    }

    public void refreshFromDatabase() {
        resetList();
    }

    private void resetList() {
        workingList = manager.getDatabase().getItemsCopy();
        tableModel.setItems(workingList);
        infoLabel.setText("Showing all " + workingList.size() + " items.");
    }

    private void liveSearch() {
        if (searchAlgoCombo.getSelectedItem() == SearchEngine.Mode.LINEAR) {
            runSearch();
        }
    }

    private void runSearch() {
        try {
            SearchEngine.Field field = (SearchEngine.Field) fieldCombo.getSelectedItem();
            SearchEngine.Mode mode = (SearchEngine.Mode) searchAlgoCombo.getSelectedItem();
            String query = queryField.getText();

            long start = System.nanoTime();
            List<LibraryItem> results = manager.getSearchEngine()
                    .search(manager.getDatabase().getItemsCopy(), query, field, mode);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            workingList = new ArrayList<>(results);
            tableModel.setItems(workingList);
            infoLabel.setText(String.format(
                    "Search [%s on %s] → %d result(s) in %d ms",
                    mode, field, results.size(), elapsed));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Search Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runSort() {
        try {
            SortEngine.Algorithm algo = (SortEngine.Algorithm) sortAlgoCombo.getSelectedItem();
            SortEngine.SortKey key = (SortEngine.SortKey) sortKeyCombo.getSelectedItem();
            boolean asc = ascendingBox.isSelected();

            long start = System.nanoTime();
            manager.getSortEngine().sort(workingList, algo, key, asc);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            tableModel.setItems(workingList);
            // Also apply sort to the live database catalogue order when sorting full list
            if (workingList.size() == manager.getDatabase().getItems().size()
                    && queryField.getText().isBlank()) {
                manager.getDatabase().getItems().clear();
                manager.getDatabase().getItems().addAll(workingList);
            }
            infoLabel.setText(String.format(
                    "Sorted with %s by %s (%s) in %d ms",
                    algo, key, asc ? "ASC" : "DESC", elapsed));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sort Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
