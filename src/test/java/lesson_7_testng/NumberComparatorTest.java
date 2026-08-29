package lesson_7_testng;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;

public class NumberComparatorTest {

    @DataProvider(name = "compareData")
    public Object[][] compareData() {
        return new Object[][]{
                {5, 3, 1},
                {3, 5, -1},
                {5, 5, 0}
        };
    }

    @Test(dataProvider = "compareData")
    public void testCompare(int a, int b, int expected) {
        Assert.assertEquals(NumberComparator.compare(a, b), expected);
    }

    @DataProvider(name = "greaterData")
    public Object[][] greaterData() {
        return new Object[][]{
                {5, 3, true},
                {3, 5, false},
                {5, 5, false}
        };
    }

    @Test(dataProvider = "greaterData")
    public void testIsGreater(int a, int b, boolean expected) {
        Assert.assertEquals(NumberComparator.isGreater(a, b), expected);
    }

    @DataProvider(name = "lessData")
    public Object[][] lessData() {
        return new Object[][]{
                {3, 5, true},
                {5, 3, false},
                {5, 5, false}
        };
    }

    @Test(dataProvider = "lessData")
    public void testIsLess(int a, int b, boolean expected) {
        Assert.assertEquals(NumberComparator.isLess(a, b), expected);
    }

    @DataProvider(name = "equalData")
    public Object[][] equalData() {
        return new Object[][]{
                {5, 5, true},
                {3, 5, false},
                {5, 3, false}
        };
    }

    @Test(dataProvider = "equalData")
    public void testIsEqual(int a, int b, boolean expected) {
        Assert.assertEquals(NumberComparator.isEqual(a, b), expected);
    }
}