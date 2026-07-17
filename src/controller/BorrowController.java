package controller;

import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;

/**
 * Handles borrow, return, and reservation workflows.
 */
public class BorrowController {
    private final LibraryDatabase database;

    public BorrowController(LibraryDatabase database) {
        this.database = database;
    }

    public void borrow(String itemId, String userId) {
        LibraryItem item = database.findById(itemId);
        UserAccount user = database.getUser(userId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        if (!item.isAvailable()) {
            database.enqueueReservation(itemId, userId);
            throw new IllegalStateException(
                    "Item currently borrowed. User added to reservation queue. Position: "
                            + database.getQueue(itemId).size());
        }
        item.borrowItem(userId);
        user.recordBorrow(itemId);
    }

    public String returnItem(String itemId) {
        LibraryItem item = database.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        if (item.isAvailable()) {
            throw new IllegalStateException("Item is not currently borrowed.");
        }
        String previousBorrower = item.getBorrowerId();
        UserAccount user = database.getUser(previousBorrower);
        if (user != null) {
            user.recordReturn(itemId);
        }
        item.returnItem();

        String nextUser = database.dequeueReservation(itemId);
        if (nextUser != null) {
            UserAccount next = database.getUser(nextUser);
            if (next != null) {
                item.borrowItem(nextUser);
                next.recordBorrow(itemId);
                return "Returned. Automatically lent to next in queue: " + next.getName() + " (" + nextUser + ")";
            }
        }
        return "Item returned successfully.";
    }

    public void reserve(String itemId, String userId) {
        if (database.findById(itemId) == null) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        if (database.getUser(userId) == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        database.enqueueReservation(itemId, userId);
    }
}
