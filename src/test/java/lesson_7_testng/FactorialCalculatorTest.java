package lesson_7_testng;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;

public class FactorialCalculatorTest {

    @Test
    public void testFactorialZero() {
        Assert.assertEquals(FactorialCalculator.calculateFactorial(0), 1);
    }

    @Test
    public void testFactorialOne() {
        Assert.assertEquals(FactorialCalculator.calculateFactorial(1), 1);
    }

    @DataProvider(name = "factorialData")
    public Object[][] factorialData() {
        return new Object[][]{
                {0, 1L},
                {1, 1L},
                {2, 2L},
                {3, 6L},
                {4, 24L},
                {5, 120L},
                {10, 3628800L}
        };
    }

    @Test(dataProvider = "factorialData")
    public void testFactorialParameterized(int input, long expected) {
        Assert.assertEquals(FactorialCalculator.calculateFactorial(input), expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFactorialNegative() {
        FactorialCalculator.calculateFactorial(-1);
    }
}