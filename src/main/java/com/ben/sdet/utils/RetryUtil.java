package com.ben.sdet.utils;

public class RetryUtil {
    
    private RetryUtil() {
        // prevent instantiation
    }

    // --- Basic retry ---
    public static <T> T execute(SupplierWithException<T> action, int attempts) {
        int tries = 0;

        while (true) {
            try {
                return action.get();
            } catch (Exception e) {
                if (++tries >= attempts) {
                    throw new RuntimeException(
                        "Retry failed after " + attempts + " attempts",
                        e
                    );
                }
            }
        }
    }

    // --- Retry with delay ---
    public static <T> T execute(SupplierWithException<T> action, int attempts, long delayMs) {
        int tries = 0;

        while (true) {
            try {
                return action.get();
            } catch (Exception e) {
                if (++tries >= attempts) {
                    throw new RuntimeException(
                        "Retry failed after " + attempts + " attempts",
                        e
                    );
                }

                sleep(delayMs);
            }
        }
    }

    // --- Sleep helper ---
    private static void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", ie);
        }
    }

    // --- Functional interface ---
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
