package lesson2_7;

public class NumberComparator {

    public static int compare(int a, int b) {
        return Integer.compare(a, b);
    }

    public static boolean isGreater(int a, int b) {
        return a > b;
    }

    public static boolean isLess(int a, int b) {
        return a < b;
    }

    public static boolean isEqual(int a, int b) {
        return a == b;
    }
}