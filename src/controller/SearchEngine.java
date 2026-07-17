package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Student-implemented search algorithms: linear, binary, and recursive.
 */
public class SearchEngine {
    public enum Mode {
        LINEAR, BINARY, RECURSIVE
    }

    public enum Field {
        TITLE, AUTHOR, TYPE, ID
    }

    /**
     * Search using the selected algorithm. Binary search requires a list sorted by the same field.
     */
    public List<LibraryItem> search(List<LibraryItem> items, String query, Field field, Mode mode) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(items);
        }
        String q = query.trim().toLowerCase(Locale.ROOT);

        return switch (mode) {
            case LINEAR -> linearSearch(items, q, field);
            case BINARY -> binarySearchAll(items, q, field);
            case RECURSIVE -> recursiveSearch(items, q, field, 0);
        };
    }

    /** 1. Linear search */
    public List<LibraryItem> linearSearch(List<LibraryItem> items, String queryLower, Field field) {
        List<LibraryItem> results = new ArrayList<>();
        for (LibraryItem item : items) {
            if (matches(item, queryLower, field)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * 2. Binary search — finds one match on a sorted list, then expands for duplicates / partial matches nearby.
     * For partial (contains) queries we fall back to scanning the sorted range.
     */
    public List<LibraryItem> binarySearchAll(List<LibraryItem> items, String queryLower, Field field) {
        List<LibraryItem> sorted = new ArrayList<>(items);
        sorted.sort(comparatorFor(field));

        // For "contains" semantics on unsorted-equality, binary locate first candidate by prefix/exact key
        int index = binarySearchIndex(sorted, queryLower, field);
        List<LibraryItem> results = new ArrayList<>();
        if (index >= 0) {
            // expand around match for equal keys / nearby contains
            int left = index;
            while (left >= 0 && matches(sorted.get(left), queryLower, field)) {
                left--;
            }
            int right = index;
            while (right < sorted.size() && matches(sorted.get(right), queryLower, field)) {
                right++;
            }
            for (int i = left + 1; i < right; i++) {
                results.add(sorted.get(i));
            }
        } else {
            // Partial match: binary search does not apply cleanly — scan sorted list (still O(n) worst case)
            // but demonstrate binary locate attempt then linear collect for contains
            for (LibraryItem item : sorted) {
                if (matches(item, queryLower, field)) {
                    results.add(item);
                }
            }
        }
        return results;
    }

    private int binarySearchIndex(List<LibraryItem> sorted, String queryLower, Field field) {
        int low = 0;
        int high = sorted.size() - 1;
        int found = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            String key = fieldValue(sorted.get(mid), field).toLowerCase(Locale.ROOT);
            int cmp = key.compareTo(queryLower);
            if (cmp == 0 || key.contains(queryLower) || queryLower.contains(key)) {
                found = mid;
                // keep looking left for first match
                high = mid - 1;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        // If exact/contains via compare path failed, try classic exact binary on title/author start
        if (found < 0) {
            low = 0;
            high = sorted.size() - 1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                String key = fieldValue(sorted.get(mid), field).toLowerCase(Locale.ROOT);
                int cmp = key.compareTo(queryLower);
                if (cmp == 0) {
                    return mid;
                } else if (cmp < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
        }
        return found;
    }

    /** 3. Recursive search by title or author (or other field). */
    public List<LibraryItem> recursiveSearch(List<LibraryItem> items, String queryLower, Field field, int index) {
        List<LibraryItem> results = new ArrayList<>();
        recursiveSearchHelper(items, queryLower, field, index, results);
        return results;
    }

    private void recursiveSearchHelper(List<LibraryItem> items, String queryLower, Field field,
                                       int index, List<LibraryItem> results) {
        if (index >= items.size()) {
            return;
        }
        if (matches(items.get(index), queryLower, field)) {
            results.add(items.get(index));
        }
        recursiveSearchHelper(items, queryLower, field, index + 1, results);
    }

    private boolean matches(LibraryItem item, String queryLower, Field field) {
        return fieldValue(item, field).toLowerCase(Locale.ROOT).contains(queryLower);
    }

    private String fieldValue(LibraryItem item, Field field) {
        return switch (field) {
            case TITLE -> item.getTitle();
            case AUTHOR -> item.getAuthor();
            case TYPE -> item.getType();
            case ID -> item.getId();
        };
    }

    private Comparator<LibraryItem> comparatorFor(Field field) {
        return switch (field) {
            case TITLE -> Comparator.comparing(i -> i.getTitle().toLowerCase(Locale.ROOT));
            case AUTHOR -> Comparator.comparing(i -> i.getAuthor().toLowerCase(Locale.ROOT));
            case TYPE -> Comparator.comparing(i -> i.getType().toLowerCase(Locale.ROOT));
            case ID -> Comparator.comparing(LibraryItem::getId);
        };
    }
}
