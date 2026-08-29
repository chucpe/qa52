package lesson2_7;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FactorialCalculatorTest {

    @Test
    @DisplayName("Test factorial of zero")
    void testFactorialZero() {
        assertEquals(1, FactorialCalculator.calculateFactorial(0));
    }

    @Test
    @DisplayName("Test factorial of one")
    void testFactorialOne() {
        assertEquals(1, FactorialCalculator.calculateFactorial(1));
    }

    @Test
    @DisplayName("Test factorial of positive numbers")
    void testFactorialPositive() {
        assertEquals(2, FactorialCalculator.calculateFactorial(2));
        assertEquals(6, FactorialCalculator.calculateFactorial(3));
        assertEquals(24, FactorialCalculator.calculateFactorial(4));
        assertEquals(120, FactorialCalculator.calculateFactorial(5));
        assertEquals(3628800, FactorialCalculator.calculateFactorial(10));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "1, 1",
            "2, 2",
            "3, 6",
            "4, 24",
            "5, 120"
    })
    @DisplayName("Parameterized test for factorial")
    void testFactorialParameterized(int input, long expected) {
        assertEquals(expected, FactorialCalculator.calculateFactorial(input));
    }

    @Test
    @DisplayName("Test negative number throws exception")
    void testFactorialNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> FactorialCalculator.calculateFactorial(-1));
    }

    @Test
    @DisplayName("Test large factorial")
    void testFactorialLarge() {
        assertTrue(FactorialCalculator.calculateFactorial(20) > 0);
    }
}