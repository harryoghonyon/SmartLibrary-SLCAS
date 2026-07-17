package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates a library user and their borrowing history (composition).
 */
public class UserAccount {
    private final String userId;
    private String name;
    private String email;
    private final List<String> borrowingHistory; // item IDs
    private final List<String> currentlyBorrowed;

    public UserAccount(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.borrowingHistory = new ArrayList<>();
        this.currentlyBorrowed = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    public List<String> getCurrentlyBorrowed() {
        return Collections.unmodifiableList(currentlyBorrowed);
    }

    public void recordBorrow(String itemId) {
        currentlyBorrowed.add(itemId);
        borrowingHistory.add(itemId);
    }

    public void recordReturn(String itemId) {
        currentlyBorrowed.remove(itemId);
    }

    public void restoreHistory(List<String> history, List<String> current) {
        borrowingHistory.clear();
        currentlyBorrowed.clear();
        if (history != null) {
            borrowingHistory.addAll(history);
        }
        if (current != null) {
            currentlyBorrowed.addAll(current);
        }
    }

    @Override
    public String toString() {
        return userId + " — " + name + " (" + email + ")";
    }
}
