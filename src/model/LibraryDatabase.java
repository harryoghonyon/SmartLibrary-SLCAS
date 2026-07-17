package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Central data store using composition: holds items, users, and reservation queues.
 */
public class LibraryDatabase {
    private final ArrayList<LibraryItem> items;
    private final Map<String, UserAccount> users;
    /** Reservation / waitlist per item ID. */
    private final Map<String, Queue<String>> reservationQueues;
    /** Fixed-size cache of most frequently accessed item IDs. */
    private final String[] frequentAccessCache;
    private int cacheSize;

    public LibraryDatabase(int cacheCapacity) {
        this.items = new ArrayList<>();
        this.users = new HashMap<>();
        this.reservationQueues = new HashMap<>();
        this.frequentAccessCache = new String[cacheCapacity];
        this.cacheSize = 0;
    }

    public ArrayList<LibraryItem> getItems() {
        return items;
    }

    public List<LibraryItem> getItemsCopy() {
        return new ArrayList<>(items);
    }

    public Map<String, UserAccount> getUsers() {
        return users;
    }

    public Map<String, Queue<String>> getReservationQueues() {
        return reservationQueues;
    }

    public void addItem(LibraryItem item) {
        items.add(item);
        reservationQueues.putIfAbsent(item.getId(), new LinkedList<>());
    }

    public boolean removeItemById(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                items.remove(i);
                reservationQueues.remove(id);
                return true;
            }
        }
        return false;
    }

    public LibraryItem findById(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) {
                recordAccess(id);
                return item;
            }
        }
        return null;
    }

    public void addUser(UserAccount user) {
        users.put(user.getUserId(), user);
    }

    public UserAccount getUser(String userId) {
        return users.get(userId);
    }

    public void enqueueReservation(String itemId, String userId) {
        reservationQueues.computeIfAbsent(itemId, k -> new LinkedList<>()).offer(userId);
    }

    public String dequeueReservation(String itemId) {
        Queue<String> q = reservationQueues.get(itemId);
        if (q == null || q.isEmpty()) {
            return null;
        }
        return q.poll();
    }

    public Queue<String> getQueue(String itemId) {
        return reservationQueues.getOrDefault(itemId, new LinkedList<>());
    }

    /** Update fixed-size most-frequently-accessed cache (simple MRU-ish insert). */
    public void recordAccess(String itemId) {
        // If already in cache, move toward front by rebuilding order lightly
        for (int i = 0; i < cacheSize; i++) {
            if (itemId.equals(frequentAccessCache[i])) {
                // shift left toward index 0
                for (int j = i; j > 0; j--) {
                    frequentAccessCache[j] = frequentAccessCache[j - 1];
                }
                frequentAccessCache[0] = itemId;
                return;
            }
        }
        if (cacheSize < frequentAccessCache.length) {
            // shift right and insert at 0
            for (int i = cacheSize; i > 0; i--) {
                frequentAccessCache[i] = frequentAccessCache[i - 1];
            }
            frequentAccessCache[0] = itemId;
            cacheSize++;
        } else {
            for (int i = frequentAccessCache.length - 1; i > 0; i--) {
                frequentAccessCache[i] = frequentAccessCache[i - 1];
            }
            frequentAccessCache[0] = itemId;
        }
    }

    public String[] getFrequentAccessCache() {
        String[] copy = new String[cacheSize];
        System.arraycopy(frequentAccessCache, 0, copy, 0, cacheSize);
        return copy;
    }

    /** Recursive count of items by category/type. */
    public int countByTypeRecursive(String type) {
        return countByTypeRecursive(0, type);
    }

    private int countByTypeRecursive(int index, String type) {
        if (index >= items.size()) {
            return 0;
        }
        int match = items.get(index).getType().equalsIgnoreCase(type) ? 1 : 0;
        return match + countByTypeRecursive(index + 1, type);
    }

    public void replaceAllItems(List<LibraryItem> newItems) {
        items.clear();
        reservationQueues.clear();
        items.addAll(newItems);
        for (LibraryItem item : items) {
            reservationQueues.putIfAbsent(item.getId(), new LinkedList<>());
        }
    }

    public List<UserAccount> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void clearUsers() {
        users.clear();
    }
}
