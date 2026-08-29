package lesson_7_testng;

public class ArithmeticCalculator {

    public static int add(int a, int b) { // вернули static
        return a + b;
    }

    public static int subtract(int a, int b) { // вернули static
        return a - b;
    }

    public static int multiply(int a, int b) { // вернули static
        return a * b;
    }

    public static double divide(int a, int b) { // вернули static
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return (double) a / b;
    }
}