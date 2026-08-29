package lesson2_7;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArithmeticCalculatorTest {

    // ===== ТЕСТЫ ДЛЯ СЛОЖЕНИЯ =====
    @ParameterizedTest
    @CsvSource({
            "2, 1, 3",
            "-1, 1, 0",
            "0, 0, 0",
            "10, -5, 5"
    })
    @DisplayName("Test addition")
    void testAdd(int a, int b, int expected) {
        assertEquals(expected, ArithmeticCalculator.add(a, b));
    }

    // ===== ТЕСТЫ ДЛЯ ВЫЧИТАНИЯ =====
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

    // ===== ТЕСТЫ ДЛЯ УМНОЖЕНИЯ =====
    @ParameterizedTest
    @CsvSource({
            "2, 3, 6",
            "-1, 5, -5",
            "0, 10, 0",
            "4, -3, -12"
    })
    @DisplayName("Test multiplication")
    void testMultiply(int a, int b, int expected) {
        assertEquals(expected, ArithmeticCalculator.multiply(a, b));
    }

    // ===== ТЕСТЫ ДЛЯ ДЕЛЕНИЯ =====
    @ParameterizedTest
    @CsvSource({
            "6, 3, 2.0",
            "5, 2, 2.5",
            "10, 4, 2.5",
            "-6, 3, -2.0"
    })
    @DisplayName("Test division")
    void testDivide(int a, int b, double expected) {
        assertEquals(expected, ArithmeticCalculator.divide(a, b), 0.001);
    }

    @Test
    @DisplayName("Test division by zero throws exception")
    void testDivideByZero() {
        assertThrows(ArithmeticException.class,
                () -> ArithmeticCalculator.divide(5, 0));
    }
}