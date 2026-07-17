package model;

/**
 * Interface for items that can be borrowed from the library.
 */
public interface Borrowable {
    boolean isAvailable();

    void borrowItem(String userId);

    void returnItem();

    String getBorrowerId();
}
