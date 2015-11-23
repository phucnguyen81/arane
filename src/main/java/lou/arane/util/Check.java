package lou.arane.util;

public class Check {

    public static <T> T notNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T notNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

    public static void require(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError(errorMessage);
        }
    }

    public static void forbid(boolean unexpected, String errorMessage) {
        if (unexpected) {
            throw new AssertionError(errorMessage);
        }
    }

    public static void postCond(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError("Post-condition violated. " + errorMessage);
        }
    }

}
