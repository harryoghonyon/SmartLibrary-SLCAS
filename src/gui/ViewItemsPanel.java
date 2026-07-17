package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * View Items tab — browse catalogue with custom cell renderer.
 */
public class ViewItemsPanel extends JPanel {
    private final LibraryManager manager;
    private final ItemTableModel tableModel;
    private final JTable table;
    private final Runnable statusUpdater;

    public ViewItemsPanel(LibraryManager manager, Runnable statusUpdater) {
        this.manager = manager;
        this.statusUpdater = statusUpdater;
        this.tableModel = new ItemTableModel();
        this.table = new JTable(tableModel) {
            @Override
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                tip.setBackground(new Color(255, 250, 240));
                return tip;
            }
        };

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel heading = new JLabel("Library Catalogue");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        heading.setToolTipText("All books, magazines, and journals in the system");

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setMnemonic('R');
        refreshBtn.setToolTipText("Reload catalogue from memory (Alt+R)");
        refreshBtn.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.add(heading, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new StatusCellRenderer());
        table.getTableHeader().setReorderingAllowed(false);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        tableModel.setItems(manager.getDatabase().getItemsCopy());
        if (statusUpdater != null) {
            statusUpdater.run();
        }
    }

    public LibraryItem getSelectedItem() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getItemAt(modelRow);
    }

    /** Custom renderer highlighting availability / overdue. */
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object status = table.getModel().getValueAt(
                        table.convertRowIndexToModel(row), 5);
                String s = String.valueOf(status);
                if ("OVERDUE".equals(s)) {
                    c.setBackground(new Color(255, 220, 220));
                    c.setForeground(new Color(140, 0, 0));
                } else if ("Borrowed".equals(s)) {
                    c.setBackground(new Color(255, 243, 205));
                    c.setForeground(Color.BLACK);
                } else if ("Available".equals(s) && column == 5) {
                    c.setBackground(new Color(220, 245, 220));
                    c.setForeground(new Color(0, 90, 0));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252));
                    c.setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
}
