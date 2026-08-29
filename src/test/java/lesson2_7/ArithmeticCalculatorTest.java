package lesson2_7;

import lesson2_7.ArithmeticCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArithmeticCalculatorTest {

    @ParameterizedTest
    @CsvSource({
            "2, 1, 3",  // Исправлено: 2 + 1 = 3
            "-1, 1, 0",
            "0, 0, 0",
            "10, -5, 5"
    })
    @DisplayName("Test addition")
    void testAdd(int a, int b, int expected) {
        assertEquals(expected, ArithmeticCalculator.add(a, b));
    }

    @ParameterizedTest
    @CsvSource({
            "5, 3, 2",
            "0, 0, 0",
            "10, 20, -10",
            "-5, -3, -2"
    })
    @DisplayName("Test subtraction")
    void testSubtract(int a, int b, int expected) {
        assertEquals(expected, ArithmeticCalculator.subtract(a, b));
    }
}