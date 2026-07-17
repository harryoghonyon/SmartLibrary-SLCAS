package utils;

/**
 * Generates unique IDs for library items and users.
 */
public class IDGenerator {
    private static int itemCounter = 1000;
    private static int userCounter = 100;

    public static synchronized String nextItemId(String typePrefix) {
        itemCounter++;
        String prefix = typePrefix == null || typePrefix.isBlank()
                ? "ITM"
                : typePrefix.substring(0, Math.min(3, typePrefix.length())).toUpperCase();
        return prefix + "-" + itemCounter;
    }

    public static synchronized String nextUserId() {
        userCounter++;
        return "USR-" + userCounter;
    }

    public static void seedItemCounter(int value) {
        if (value > itemCounter) {
            itemCounter = value;
        }
    }

    public static void seedUserCounter(int value) {
        if (value > userCounter) {
            userCounter = value;
        }
    }
}
