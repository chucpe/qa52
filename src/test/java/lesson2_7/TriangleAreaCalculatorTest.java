package lesson2_7;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TriangleAreaCalculatorTest {

    @Test
    @DisplayName("Test area with base and height")
    void testAreaBaseHeight() {
        assertEquals(10.0, TriangleAreaCalculator.calculateArea(4, 5));
        assertEquals(7.5, TriangleAreaCalculator.calculateArea(3, 5));
        assertEquals(25.0, TriangleAreaCalculator.calculateArea(10, 5));
    }

    @ParameterizedTest
    @CsvSource({
            "4, 5, 10.0",
            "3, 5, 7.5",
            "10, 5, 25.0",
            "2, 3, 3.0"
    })
    @DisplayName("Parameterized test for area with base and height")
    void testAreaBaseHeightParameterized(double base, double height, double expected) {
        assertEquals(expected, TriangleAreaCalculator.calculateArea(base, height));
    }

    @Test
    @DisplayName("Test Heron's formula")
    void testHeronFormula() {
        assertEquals(6.0, TriangleAreaCalculator.calculateAreaByHeron(3, 4, 5), 0.001);
        assertEquals(9.9216, TriangleAreaCalculator.calculateAreaByHeron(5, 6, 7), 0.001);
    }

    @Test
    @DisplayName("Test invalid triangle sides")
    void testInvalidTriangle() {
        assertThrows(IllegalArgumentException.class,
                () -> TriangleAreaCalculator.calculateAreaByHeron(1, 1, 3));
    }

    @Test
    @DisplayName("Test negative base throws exception")
    void testNegativeBase() {
        assertThrows(IllegalArgumentException.class,
                () -> TriangleAreaCalculator.calculateArea(-4, 5));
    }
}
