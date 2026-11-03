package impacthub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {
    @Test
    void textIsTrimmed() {
        assertEquals("Sara", Validation.requireText("  Sara ", "The name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankTextIsRefusedWithTheLabelInTheMessage(String value) {
        IllegalArgumentException refused =
                assertThrows(IllegalArgumentException.class, () -> Validation.requireText(value, "The name"));
        assertEquals("The name is required.", refused.getMessage());
    }

    @Test
    void textLongerThan100CharactersIsRefused() {
        assertDoesNotThrow(() -> Validation.requireText("x".repeat(100), "The name"));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireText("x".repeat(101), "The name"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 24.5})
    void numbersOutsideTheRangeAreRefused(double value) {
        assertThrows(IllegalArgumentException.class, () -> Validation.requirePositive(value, 24, "Hours"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.5, 1, 24})
    void numbersInsideTheRangeAreAccepted(double value) {
        assertDoesNotThrow(() -> Validation.requirePositive(value, 24, "Hours"));
    }

    @Test
    void datesMustBeInTheFuture() {
        assertDoesNotThrow(() -> Validation.requireFutureDate(LocalDateTime.now().plusMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireFutureDate(LocalDateTime.now().minusMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireFutureDate(null));
    }
}
