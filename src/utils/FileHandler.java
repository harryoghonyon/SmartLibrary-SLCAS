package utils;

import model.Book;
import model.Journal;
import model.LibraryDatabase;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Saves and loads library data using a simple pipe-delimited text / JSON-like format.
 */
public class FileHandler {
    private final String dataPath;

    public FileHandler(String dataPath) {
        this.dataPath = dataPath;
    }

    public void save(LibraryDatabase db) throws IOException {
        File file = new File(dataPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# SLCAS Data File");
            writer.newLine();

            for (LibraryItem item : db.getItems()) {
                writer.write(serializeItem(item));
                writer.newLine();
            }

            for (UserAccount user : db.getAllUsers()) {
                writer.write(serializeUser(user));
                writer.newLine();
            }

            for (var entry : db.getReservationQueues().entrySet()) {
                Queue<String> q = entry.getValue();
                if (q != null && !q.isEmpty()) {
                    writer.write("QUEUE|" + entry.getKey() + "|" + String.join(",", q));
                    writer.newLine();
                }
            }
        }
    }

    public void load(LibraryDatabase db) throws IOException {
        File file = new File(dataPath);
        if (!file.exists()) {
            return;
        }

        List<LibraryItem> items = new ArrayList<>();
        db.clearUsers();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                switch (parts[0]) {
                    case "BOOK", "MAGAZINE", "JOURNAL" -> {
                        LibraryItem item = deserializeItem(parts);
                        if (item != null) {
                            items.add(item);
                            seedItemId(item.getId());
                        }
                    }
                    case "USER" -> {
                        UserAccount user = deserializeUser(parts);
                        if (user != null) {
                            db.addUser(user);
                            seedUserId(user.getUserId());
                        }
                    }
                    case "QUEUE" -> {
                        if (parts.length >= 3) {
                            String itemId = parts[1];
                            Queue<String> q = new LinkedList<>();
                            if (!parts[2].isBlank()) {
                                q.addAll(Arrays.asList(parts[2].split(",")));
                            }
                            db.getReservationQueues().put(itemId, q);
                        }
                    }
                    default -> {
                        // ignore unknown
                    }
                }
            }
        }

        db.replaceAllItems(items);
        // re-apply queues that may have been cleared by replaceAllItems
        // (replaceAllItems creates empty queues — reload queues from file again)
        reloadQueuesOnly(db, file);
    }

    private void reloadQueuesOnly(LibraryDatabase db, File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("QUEUE|")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    Queue<String> q = new LinkedList<>();
                    if (!parts[2].isBlank()) {
                        q.addAll(Arrays.asList(parts[2].split(",")));
                    }
                    db.getReservationQueues().put(parts[1], q);
                }
            }
        }
    }

    private String serializeItem(LibraryItem item) {
        String due = item.getDueDate() == null ? "null" : item.getDueDate().toString();
        String borrower = item.getBorrowerId() == null ? "null" : item.getBorrowerId();
        String base = String.join("|",
                item.getType().toUpperCase(),
                item.getId(),
                escape(item.getTitle()),
                escape(item.getAuthor()),
                String.valueOf(item.getYear()),
                String.valueOf(item.isAvailable()),
                borrower,
                String.valueOf(item.getBorrowCount()),
                due);

        if (item instanceof Book book) {
            return base + "|" + escape(book.getIsbn()) + "|" + book.getPages();
        } else if (item instanceof Magazine mag) {
            return base + "|" + mag.getIssueNumber() + "|" + escape(mag.getFrequency());
        } else if (item instanceof Journal journal) {
            return base + "|" + escape(journal.getVolume()) + "|" + escape(journal.getDoi());
        }
        return base;
    }

    private LibraryItem deserializeItem(String[] p) {
        // TYPE|id|title|author|year|available|borrower|borrowCount|due|extra1|extra2
        if (p.length < 11) {
            return null;
        }
        String type = p[0];
        String id = p[1];
        String title = unescape(p[2]);
        String author = unescape(p[3]);
        int year = Integer.parseInt(p[4]);
        boolean available = Boolean.parseBoolean(p[5]);
        String borrower = "null".equals(p[6]) ? null : p[6];
        int borrowCount = Integer.parseInt(p[7]);
        String due = p[8];

        LibraryItem item;
        switch (type) {
            case "BOOK" -> item = new Book(id, title, author, year, unescape(p[9]), Integer.parseInt(p[10]));
            case "MAGAZINE" -> item = new Magazine(id, title, author, year, Integer.parseInt(p[9]), unescape(p[10]));
            case "JOURNAL" -> item = new Journal(id, title, author, year, unescape(p[9]), unescape(p[10]));
            default -> {
                return null;
            }
        }
        item.restoreBorrowState(available, borrower, borrowCount, due);
        return item;
    }

    private String serializeUser(UserAccount user) {
        return String.join("|",
                "USER",
                user.getUserId(),
                escape(user.getName()),
                escape(user.getEmail()),
                String.join(",", user.getBorrowingHistory()),
                String.join(",", user.getCurrentlyBorrowed()));
    }

    private UserAccount deserializeUser(String[] p) {
        if (p.length < 6) {
            return null;
        }
        UserAccount user = new UserAccount(p[1], unescape(p[2]), unescape(p[3]));
        List<String> history = p[4].isBlank() ? List.of() : Arrays.asList(p[4].split(","));
        List<String> current = p[5].isBlank() ? List.of() : Arrays.asList(p[5].split(","));
        user.restoreHistory(history, current);
        return user;
    }

    private void seedItemId(String id) {
        int dash = id.lastIndexOf('-');
        if (dash >= 0) {
            try {
                IDGenerator.seedItemCounter(Integer.parseInt(id.substring(dash + 1)));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void seedUserId(String id) {
        int dash = id.lastIndexOf('-');
        if (dash >= 0) {
            try {
                IDGenerator.seedUserCounter(Integer.parseInt(id.substring(dash + 1)));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("|", "/").replace("\n", " ");
    }

    private static String unescape(String s) {
        return s == null ? "" : s;
    }

    public String getDataPath() {
        return dataPath;
    }
}
