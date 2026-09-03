package lesson2_7;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class NumberComparatorTest {

    @ParameterizedTest
    @CsvSource({
            "5, 3, 1",
            "3, 5, -1",
            "5, 5, 0"
    })
    @DisplayName("Test compare method")
    void testCompare(int a, int b, int expected) {
        assertEquals(expected, NumberComparator.compare(a, b));
    }

    @ParameterizedTest
    @CsvSource({
            "5, 3, true",
            "3, 5, false",
            "5, 5, false"
    })
    @DisplayName("Test isGreater method")
    void testIsGreater(int a, int b, boolean expected) {
        assertEquals(expected, NumberComparator.isGreater(a, b));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 5, true",
            "5, 3, false",
            "5, 5, false"
    })
    @DisplayName("Test isLess method")
    void testIsLess(int a, int b, boolean expected) {
        assertEquals(expected, NumberComparator.isLess(a, b));
    }

    @ParameterizedTest
    @CsvSource({
            "5, 5, true",
            "3, 5, false",
            "5, 3, false"
    })
    @DisplayName("Test isEqual method")
    void testIsEqual(int a, int b, boolean expected) {
        assertEquals(expected, NumberComparator.isEqual(a, b));
    }
}

