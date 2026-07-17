package controller;

import model.Book;
import model.Journal;
import model.LibraryDatabase;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;
import utils.FileHandler;
import utils.IDGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Facade / manager for library operations, undo stack, reports, and polymorphism demo.
 */
public class LibraryManager {
    private final LibraryDatabase database;
    private final BorrowController borrowController;
    private final SearchEngine searchEngine;
    private final SortEngine sortEngine;
    private final FileHandler fileHandler;
    private final Stack<UndoAction> undoStack;

    public LibraryManager(String dataFilePath) {
        this.database = new LibraryDatabase(5);
        this.borrowController = new BorrowController(database);
        this.searchEngine = new SearchEngine();
        this.sortEngine = new SortEngine();
        this.fileHandler = new FileHandler(dataFilePath);
        this.undoStack = new Stack<>();
    }

    public LibraryDatabase getDatabase() {
        return database;
    }

    public BorrowController getBorrowController() {
        return borrowController;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public SortEngine getSortEngine() {
        return sortEngine;
    }

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    /** Polymorphic processor — works on any LibraryItem subtype. */
    public void processLibraryItem(LibraryItem item, Consumer<String> logger) {
        String status = item.isAvailable() ? "AVAILABLE" : "ON LOAN to " + item.getBorrowerId();
        logger.accept(String.format(
                "Processing %s | ID=%s | \"%s\" by %s (%d) | %s | %s",
                item.getType(), item.getId(), item.getTitle(), item.getAuthor(),
                item.getYear(), item.getExtraInfo(), status));
        if (item.isOverdue()) {
            logger.accept("  Overdue charge: ₦" + String.format("%.2f", item.computeOverdueCharge()));
        }
    }

    public void processAllItems(Consumer<String> logger) {
        for (LibraryItem item : database.getItems()) {
            processLibraryItem(item, logger);
        }
    }

    public LibraryItem addBook(String title, String author, int year, String isbn, int pages) {
        Book book = new Book(IDGenerator.nextItemId("Book"), title, author, year, isbn, pages);
        database.addItem(book);
        undoStack.push(UndoAction.deleteItem(book.getId()));
        return book;
    }

    public LibraryItem addMagazine(String title, String author, int year, int issue, String frequency) {
        Magazine mag = new Magazine(IDGenerator.nextItemId("Magazine"), title, author, year, issue, frequency);
        database.addItem(mag);
        undoStack.push(UndoAction.deleteItem(mag.getId()));
        return mag;
    }

    public Journal addJournal(String title, String author, int year, String volume, String doi) {
        Journal journal = new Journal(IDGenerator.nextItemId("Journal"), title, author, year, volume, doi);
        database.addItem(journal);
        undoStack.push(UndoAction.deleteItem(journal.getId()));
        return journal;
    }

    public LibraryItem deleteItem(String itemId) {
        LibraryItem item = database.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        // snapshot for undo
        undoStack.push(UndoAction.restoreItem(cloneItem(item)));
        database.removeItemById(itemId);
        return item;
    }

    public UserAccount addUser(String name, String email) {
        UserAccount user = new UserAccount(IDGenerator.nextUserId(), name, email);
        database.addUser(user);
        undoStack.push(UndoAction.deleteUser(user.getUserId()));
        return user;
    }

    public String undoLastAdminAction() {
        if (undoStack.isEmpty()) {
            return "Nothing to undo.";
        }
        UndoAction action = undoStack.pop();
        return action.apply(database);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void save() throws IOException {
        fileHandler.save(database);
    }

    public void load() throws IOException {
        fileHandler.load(database);
        undoStack.clear();
    }

    public void loadSeedDataIfEmpty() {
        if (!database.getItems().isEmpty()) {
            return;
        }
        addBook("Introduction to Algorithms", "Cormen et al.", 2009, "978-0262033848", 1312);
        addBook("Clean Code", "Robert C. Martin", 2008, "978-0132350884", 464);
        addBook("Effective Java", "Joshua Bloch", 2018, "978-0134685991", 412);
        addMagazine("IEEE Spectrum", "IEEE", 2024, 3, "Monthly");
        addMagazine("Communications of the ACM", "ACM", 2023, 11, "Monthly");
        addJournal("Journal of Systems Software", "Elsevier", 2022, "190", "10.1016/j.jss.2022");
        addJournal("IEEE Trans. on Computers", "IEEE", 2021, "70-4", "10.1109/TC.2021");
        addUser("Ada Lovelace", "ada@university.edu");
        addUser("Alan Turing", "turing@university.edu");
        addUser("Grace Hopper", "hopper@university.edu");
        undoStack.clear();
    }

    // ---- Reports ----

    public List<LibraryItem> mostBorrowedItems(int limit) {
        return database.getItemsCopy().stream()
                .sorted(Comparator.comparingInt(LibraryItem::getBorrowCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> usersWithOverdueItems() {
        List<String> lines = new ArrayList<>();
        for (LibraryItem item : database.getItems()) {
            if (item.isOverdue()) {
                UserAccount u = database.getUser(item.getBorrowerId());
                String name = u == null ? item.getBorrowerId() : u.getName();
                lines.add(String.format("%s (%s) — \"%s\" due %s — charge ₦%.2f",
                        name, item.getBorrowerId(), item.getTitle(), item.getDueDate(),
                        item.computeOverdueCharge()));
            }
        }
        return lines;
    }

    public Map<String, Integer> categoryDistribution() {
        Map<String, Integer> map = new HashMap<>();
        map.put("Book", database.countByTypeRecursive("Book"));
        map.put("Magazine", database.countByTypeRecursive("Magazine"));
        map.put("Journal", database.countByTypeRecursive("Journal"));
        return map;
    }

    private LibraryItem cloneItem(LibraryItem item) {
        LibraryItem copy;
        if (item instanceof Book b) {
            copy = new Book(b.getId(), b.getTitle(), b.getAuthor(), b.getYear(), b.getIsbn(), b.getPages());
        } else if (item instanceof Magazine m) {
            copy = new Magazine(m.getId(), m.getTitle(), m.getAuthor(), m.getYear(), m.getIssueNumber(), m.getFrequency());
        } else if (item instanceof Journal j) {
            copy = new Journal(j.getId(), j.getTitle(), j.getAuthor(), j.getYear(), j.getVolume(), j.getDoi());
        } else {
            throw new IllegalArgumentException("Unknown item type");
        }
        String due = item.getDueDate() == null ? null : item.getDueDate().toString();
        copy.restoreBorrowState(item.isAvailable(), item.getBorrowerId(), item.getBorrowCount(), due);
        return copy;
    }

    /** Undo action stored on a Stack. */
    public static class UndoAction {
        enum Type { DELETE_ITEM, RESTORE_ITEM, DELETE_USER }

        private final Type type;
        private final String id;
        private final LibraryItem snapshot;

        private UndoAction(Type type, String id, LibraryItem snapshot) {
            this.type = type;
            this.id = id;
            this.snapshot = snapshot;
        }

        static UndoAction deleteItem(String id) {
            return new UndoAction(Type.DELETE_ITEM, id, null);
        }

        static UndoAction restoreItem(LibraryItem snapshot) {
            return new UndoAction(Type.RESTORE_ITEM, snapshot.getId(), snapshot);
        }

        static UndoAction deleteUser(String id) {
            return new UndoAction(Type.DELETE_USER, id, null);
        }

        String apply(LibraryDatabase db) {
            return switch (type) {
                case DELETE_ITEM -> {
                    db.removeItemById(id);
                    yield "Undo: removed newly added item " + id;
                }
                case RESTORE_ITEM -> {
                    db.addItem(snapshot);
                    yield "Undo: restored deleted item " + snapshot.getTitle();
                }
                case DELETE_USER -> {
                    db.getUsers().remove(id);
                    yield "Undo: removed newly added user " + id;
                }
            };
        }
    }
}
