package lesson_7_testng;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;

public class ArithmeticCalculatorTest {

    // ===== ДАННЫЕ ДЛЯ СЛОЖЕНИЯ =====
    @DataProvider(name = "additionData")
    public Object[][] additionData() {
        return new Object[][]{
                {2, 1, 3},
                {-1, 1, 0},
                {0, 0, 0},
                {10, -5, 5}
        };
    }

    @Test(dataProvider = "additionData")
    public void testAdd(int a, int b, int expected) {
        Assert.assertEquals(ArithmeticCalculator.add(a, b), expected);
    }

    // ===== ДАННЫЕ ДЛЯ ВЫЧИТАНИЯ =====
    @DataProvider(name = "subtractionData")
    public Object[][] subtractionData() {
        return new Object[][]{
                {5, 3, 2},
                {0, 0, 0},
                {10, 20, -10},
                {-5, -3, -2}
        };
    }

    @Test(dataProvider = "subtractionData")
    public void testSubtract(int a, int b, int expected) {
        Assert.assertEquals(ArithmeticCalculator.subtract(a, b), expected);
    }

    // ===== ДАННЫЕ ДЛЯ УМНОЖЕНИЯ =====
    @DataProvider(name = "multiplicationData")
    public Object[][] multiplicationData() {
        return new Object[][]{
                {2, 3, 6},
                {-1, 5, -5},
                {0, 10, 0},
                {4, -3, -12}
        };
    }

    @Test(dataProvider = "multiplicationData")
    public void testMultiply(int a, int b, int expected) {
        Assert.assertEquals(ArithmeticCalculator.multiply(a, b), expected);
    }

    // ===== ДАННЫЕ ДЛЯ ДЕЛЕНИЯ =====
    @DataProvider(name = "divisionData")
    public Object[][] divisionData() {
        return new Object[][]{
                {6, 3, 2.0},
                {5, 2, 2.5},
                {10, 4, 2.5},
                {-6, 3, -2.0}
        };
    }

    @Test(dataProvider = "divisionData")
    public void testDivide(int a, int b, double expected) {
        Assert.assertEquals(ArithmeticCalculator.divide(a, b), expected, 0.001);
    }

    // ===== ТЕСТ НА ДЕЛЕНИЕ НА НОЛЬ =====
    @Test(expectedExceptions = ArithmeticException.class)
    public void testDivideByZero() {
        ArithmeticCalculator.divide(5, 0);
    }
}