package utils.data;

public class TestDataUtils {

    /*
     * Generates a unique email by inserting a timestamp before the "@" symbol.
     * If the input is not a valid email, it appends the timestamp at the end.
     * This allows us to reuse the same base email in test data while ensuring uniqueness for each test run.
     */
    public static String uniqueEmail(String base) {
        if (base == null || base.isEmpty()) {
            return base; // Let validation handle null/empty cases
        }

        String unique = "_" + System.nanoTime();

        if (base.contains("@")) {
            return base.replace("@", unique + "@");
        }

        // invalid email → append safely without fixing format
        return base + unique;
    }
}
