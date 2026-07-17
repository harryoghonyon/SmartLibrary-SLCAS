package model;

/**
 * Abstract base class for all library resources.
 */
public abstract class LibraryItem implements Borrowable, Comparable<LibraryItem> {
    private final String id;
    private String title;
    private String author;
    private int year;
    private boolean available;
    private String borrowerId;
    private int borrowCount;
    private java.time.LocalDate dueDate;

    protected LibraryItem(String id, String title, String author, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = true;
        this.borrowerId = null;
        this.borrowCount = 0;
        this.dueDate = null;
    }

    public abstract String getType();

    public abstract String getExtraInfo();

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    public java.time.LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(java.time.LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getBorrowerId() {
        return borrowerId;
    }

    @Override
    public void borrowItem(String userId) {
        if (!available) {
            throw new IllegalStateException("Item is not available: " + title);
        }
        this.available = false;
        this.borrowerId = userId;
        this.borrowCount++;
        this.dueDate = java.time.LocalDate.now().plusDays(14);
    }

    @Override
    public void returnItem() {
        this.available = true;
        this.borrowerId = null;
        this.dueDate = null;
    }

    /** Restore borrow state when loading from file. */
    public void restoreBorrowState(boolean available, String borrowerId, int borrowCount, String dueDateIso) {
        this.available = available;
        this.borrowerId = borrowerId;
        this.borrowCount = borrowCount;
        this.dueDate = (dueDateIso == null || dueDateIso.isBlank() || "null".equals(dueDateIso))
                ? null
                : java.time.LocalDate.parse(dueDateIso);
    }

    public boolean isOverdue() {
        return !available && dueDate != null && java.time.LocalDate.now().isAfter(dueDate);
    }

    /** Recursive overdue charge: Naira 50 per day overdue. */
    public double computeOverdueCharge() {
        if (!isOverdue()) {
            return 0.0;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(dueDate, java.time.LocalDate.now());
        return computeOverdueChargeRecursive((int) days);
    }

    private double computeOverdueChargeRecursive(int daysLeft) {
        if (daysLeft <= 0) {
            return 0.0;
        }
        return 50.0 + computeOverdueChargeRecursive(daysLeft - 1);
    }

    @Override
    public int compareTo(LibraryItem other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s — %s (%d) [%s]",
                getType(), title, author, year, available ? "Available" : "Borrowed");
    }
}
