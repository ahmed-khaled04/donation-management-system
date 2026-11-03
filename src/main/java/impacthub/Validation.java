package impacthub;

import java.time.LocalDateTime;

/** The input rules shared by the services. Each one throws IllegalArgumentException with a message for the user. */
public final class Validation {
    private static final int MAX_TEXT_LENGTH = 100;

    private Validation() {
    }

    /** Returns the text without surrounding spaces. */
    public static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        String text = value.trim();
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(label + " cannot be longer than " + MAX_TEXT_LENGTH + " characters.");
        }
        return text;
    }

    /** Rejects zero, negative numbers, NaN, Infinity and anything above max. */
    public static void requirePositive(double value, double max, String label) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(label + " must be more than 0.");
        }
        if (value > max) {
            throw new IllegalArgumentException(label + " cannot be more than " + (long) max + ".");
        }
    }

    public static void requireFutureDate(LocalDateTime date) {
        if (date == null || !date.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("The date must be in the future.");
        }
    }
}
