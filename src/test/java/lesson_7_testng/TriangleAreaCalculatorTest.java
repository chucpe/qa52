package lesson_7_testng;

import lesson_7_testng.TriangleAreaCalculator;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;

public class TriangleAreaCalculatorTest {

    @Test
    public void testAreaBaseHeight() {
        Assert.assertEquals(TriangleAreaCalculator.calculateArea(4, 5), 10.0);
        Assert.assertEquals(TriangleAreaCalculator.calculateArea(3, 5), 7.5);
        Assert.assertEquals(TriangleAreaCalculator.calculateArea(10, 5), 25.0);
    }

    @DataProvider(name = "areaData")
    public Object[][] areaData() {
        return new Object[][]{
                {4.0, 5.0, 10.0},
                {3.0, 5.0, 7.5},
                {10.0, 5.0, 25.0},
                {2.0, 3.0, 3.0}
        };
    }

    @Test(dataProvider = "areaData")
    public void testAreaBaseHeightParameterized(double base, double height, double expected) {
        Assert.assertEquals(TriangleAreaCalculator.calculateArea(base, height), expected);
    }

    @Test
    public void testHeronFormula() {
        Assert.assertEquals(TriangleAreaCalculator.calculateAreaByHeron(3, 4, 5), 6.0, 0.001);
        Assert.assertEquals(TriangleAreaCalculator.calculateAreaByHeron(5, 6, 7), 14.6969, 0.001);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTriangle() {
        TriangleAreaCalculator.calculateAreaByHeron(1, 1, 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeBase() {
        TriangleAreaCalculator.calculateArea(-4, 5);
    }
}