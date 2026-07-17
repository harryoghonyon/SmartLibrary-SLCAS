package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Student-implemented sorting algorithms with selectable keys.
 */
public class SortEngine {
    public enum Algorithm {
        SELECTION, INSERTION, MERGE, QUICK
    }

    public enum SortKey {
        TITLE, AUTHOR, YEAR
    }

    public void sort(List<LibraryItem> items, Algorithm algorithm, SortKey key, boolean ascending) {
        Comparator<LibraryItem> cmp = comparatorFor(key);
        if (!ascending) {
            cmp = cmp.reversed();
        }
        switch (algorithm) {
            case SELECTION -> selectionSort(items, cmp);
            case INSERTION -> insertionSort(items, cmp);
            case MERGE -> {
                List<LibraryItem> sorted = mergeSort(new ArrayList<>(items), cmp);
                items.clear();
                items.addAll(sorted);
            }
            case QUICK -> quickSort(items, 0, items.size() - 1, cmp);
        }
    }

    /** 1. Selection Sort */
    public void selectionSort(List<LibraryItem> items, Comparator<LibraryItem> cmp) {
        int n = items.size();
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(items.get(j), items.get(min)) < 0) {
                    min = j;
                }
            }
            if (min != i) {
                swap(items, i, min);
            }
        }
    }

    /** 2. Insertion Sort */
    public void insertionSort(List<LibraryItem> items, Comparator<LibraryItem> cmp) {
        for (int i = 1; i < items.size(); i++) {
            LibraryItem key = items.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(items.get(j), key) > 0) {
                items.set(j + 1, items.get(j));
                j--;
            }
            items.set(j + 1, key);
        }
    }

    /** 3. Merge Sort (recommended) */
    public List<LibraryItem> mergeSort(List<LibraryItem> items, Comparator<LibraryItem> cmp) {
        if (items.size() <= 1) {
            return items;
        }
        int mid = items.size() / 2;
        List<LibraryItem> left = mergeSort(new ArrayList<>(items.subList(0, mid)), cmp);
        List<LibraryItem> right = mergeSort(new ArrayList<>(items.subList(mid, items.size())), cmp);
        return merge(left, right, cmp);
    }

    private List<LibraryItem> merge(List<LibraryItem> left, List<LibraryItem> right, Comparator<LibraryItem> cmp) {
        List<LibraryItem> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        while (i < left.size()) {
            result.add(left.get(i++));
        }
        while (j < right.size()) {
            result.add(right.get(j++));
        }
        return result;
    }

    /** 4. Quick Sort */
    public void quickSort(List<LibraryItem> items, int low, int high, Comparator<LibraryItem> cmp) {
        if (low < high) {
            int p = partition(items, low, high, cmp);
            quickSort(items, low, p - 1, cmp);
            quickSort(items, p + 1, high, cmp);
        }
    }

    private int partition(List<LibraryItem> items, int low, int high, Comparator<LibraryItem> cmp) {
        LibraryItem pivot = items.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(items.get(j), pivot) <= 0) {
                i++;
                swap(items, i, j);
            }
        }
        swap(items, i + 1, high);
        return i + 1;
    }

    private void swap(List<LibraryItem> items, int i, int j) {
        LibraryItem tmp = items.get(i);
        items.set(i, items.get(j));
        items.set(j, tmp);
    }

    private Comparator<LibraryItem> comparatorFor(SortKey key) {
        return switch (key) {
            case TITLE -> Comparator.comparing(i -> i.getTitle().toLowerCase(Locale.ROOT));
            case AUTHOR -> Comparator.comparing(i -> i.getAuthor().toLowerCase(Locale.ROOT));
            case YEAR -> Comparator.comparingInt(LibraryItem::getYear);
        };
    }
}
