package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Queue;

/**
 * Borrow / Return tab with reservation queue support.
 */
public class BorrowPanel extends JPanel {
    private final LibraryManager manager;
    private final Runnable onDataChanged;

    private final JComboBox<String> itemCombo = new JComboBox<>();
    private final JComboBox<String> userCombo = new JComboBox<>();
    private final JTextField itemIdField = new JTextField(12);
    private final JTextField userIdField = new JTextField(12);
    private final JTextArea logArea = new JTextArea(10, 40);

    public BorrowPanel(LibraryManager manager, Runnable onDataChanged) {
        this.manager = manager;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Borrow / Return / Reserve"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Select Item:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(itemCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Or Item ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        itemIdField.setToolTipText("Enter item ID directly, e.g. BOO-1001");
        form.add(itemIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Select User:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(userCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Or User ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        userIdField.setToolTipText("Enter user ID directly, e.g. USR-101");
        form.add(userIdField, gbc);

        JPanel buttons = new JPanel();
        JButton borrowBtn = new JButton("Borrow");
        borrowBtn.setMnemonic('B');
        borrowBtn.setToolTipText("Borrow selected item (Alt+B)");
        JButton returnBtn = new JButton("Return");
        returnBtn.setMnemonic('T');
        returnBtn.setToolTipText("Return selected item (Alt+T)");
        JButton reserveBtn = new JButton("Reserve / Waitlist");
        reserveBtn.setToolTipText("Join reservation queue if item is out");
        JButton queueBtn = new JButton("View Queue");
        queueBtn.setToolTipText("Show reservation queue for selected item");

        borrowBtn.addActionListener(e -> doBorrow());
        returnBtn.addActionListener(e -> doReturn());
        reserveBtn.addActionListener(e -> doReserve());
        queueBtn.addActionListener(e -> showQueue());

        buttons.add(borrowBtn);
        buttons.add(returnBtn);
        buttons.add(reserveBtn);
        buttons.add(queueBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(buttons, gbc);

        itemCombo.addActionListener(e -> {
            Object sel = itemCombo.getSelectedItem();
            if (sel != null) {
                String s = sel.toString();
                int idx = s.indexOf(' ');
                if (idx > 0) {
                    itemIdField.setText(s.substring(0, idx));
                }
            }
        });
        userCombo.addActionListener(e -> {
            Object sel = userCombo.getSelectedItem();
            if (sel != null) {
                String s = sel.toString();
                int idx = s.indexOf(' ');
                if (idx > 0) {
                    userIdField.setText(s.substring(0, idx));
                }
            }
        });

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setBorder(BorderFactory.createTitledBorder("Activity Log"));

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        refreshCombos();
    }

    public void refreshCombos() {
        itemCombo.removeAllItems();
        for (LibraryItem item : manager.getDatabase().getItems()) {
            itemCombo.addItem(item.getId() + " — " + item.getTitle()
                    + (item.isAvailable() ? " [Available]" : " [Out]"));
        }
        userCombo.removeAllItems();
        for (UserAccount user : manager.getDatabase().getAllUsers()) {
            userCombo.addItem(user.getUserId() + " — " + user.getName());
        }
    }

    private String resolveItemId() {
        String id = itemIdField.getText().trim();
        if (!id.isEmpty()) {
            return id;
        }
        Object sel = itemCombo.getSelectedItem();
        if (sel == null) {
            return null;
        }
        return sel.toString().split(" ")[0];
    }

    private String resolveUserId() {
        String id = userIdField.getText().trim();
        if (!id.isEmpty()) {
            return id;
        }
        Object sel = userCombo.getSelectedItem();
        if (sel == null) {
            return null;
        }
        return sel.toString().split(" ")[0];
    }

    private void doBorrow() {
        try {
            String itemId = resolveItemId();
            String userId = resolveUserId();
            validateIds(itemId, userId);
            manager.getBorrowController().borrow(itemId, userId);
            log("Borrowed " + itemId + " by " + userId);
            JOptionPane.showMessageDialog(this, "Borrow successful.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            notifyChanged();
        } catch (Exception ex) {
            log("Borrow error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Borrow",
                    JOptionPane.WARNING_MESSAGE);
            notifyChanged();
        }
    }

    private void doReturn() {
        try {
            String itemId = resolveItemId();
            if (itemId == null || itemId.isBlank()) {
                throw new IllegalArgumentException("Please select or enter an item ID.");
            }
            String msg = manager.getBorrowController().returnItem(itemId);
            log(msg);
            JOptionPane.showMessageDialog(this, msg, "Return", JOptionPane.INFORMATION_MESSAGE);
            notifyChanged();
        } catch (Exception ex) {
            log("Return error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doReserve() {
        try {
            String itemId = resolveItemId();
            String userId = resolveUserId();
            validateIds(itemId, userId);
            manager.getBorrowController().reserve(itemId, userId);
            log("Reserved " + itemId + " for " + userId);
            JOptionPane.showMessageDialog(this, "Added to reservation queue.", "Reserve",
                    JOptionPane.INFORMATION_MESSAGE);
            notifyChanged();
        } catch (Exception ex) {
            log("Reserve error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reserve Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQueue() {
        String itemId = resolveItemId();
        if (itemId == null || itemId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select an item first.", "Queue",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Queue<String> q = manager.getDatabase().getQueue(itemId);
        StringBuilder sb = new StringBuilder("Reservation queue for " + itemId + ":\n");
        if (q.isEmpty()) {
            sb.append("(empty)");
        } else {
            int i = 1;
            for (String uid : q) {
                UserAccount u = manager.getDatabase().getUser(uid);
                sb.append(i++).append(". ").append(uid);
                if (u != null) {
                    sb.append(" — ").append(u.getName());
                }
                sb.append("\n");
            }
        }
        log(sb.toString());
        JOptionPane.showMessageDialog(this, sb.toString(), "Reservation Queue",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void validateIds(String itemId, String userId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Please select or enter an item ID.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Please select or enter a user ID.");
        }
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private void notifyChanged() {
        refreshCombos();
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
