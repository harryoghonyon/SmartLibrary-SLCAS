package gui;

import model.LibraryItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model with a custom-friendly column layout for library items.
 */
public class ItemTableModel extends AbstractTableModel {
    private final String[] columns = {
            "ID", "Type", "Title", "Author", "Year", "Status", "Borrower", "Borrows", "Extra"
    };
    private final List<LibraryItem> rows = new ArrayList<>();

    public void setItems(List<LibraryItem> items) {
        rows.clear();
        if (items != null) {
            rows.addAll(items);
        }
        fireTableDataChanged();
    }

    public LibraryItem getItemAt(int row) {
        if (row < 0 || row >= rows.size()) {
            return null;
        }
        return rows.get(row);
    }

    public List<LibraryItem> getItems() {
        return new ArrayList<>(rows);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LibraryItem item = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getId();
            case 1 -> item.getType();
            case 2 -> item.getTitle();
            case 3 -> item.getAuthor();
            case 4 -> item.getYear();
            case 5 -> item.isAvailable() ? "Available" : (item.isOverdue() ? "OVERDUE" : "Borrowed");
            case 6 -> item.getBorrowerId() == null ? "—" : item.getBorrowerId();
            case 7 -> item.getBorrowCount();
            case 8 -> item.getExtraInfo();
            default -> "";
        };
    }
}
