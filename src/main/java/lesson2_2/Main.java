package lesson2_2;

import org.w3c.dom.ls.LSOutput;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("\n--- Задание 1: printThreeWords ---");
        printThreeWords();

        System.out.println("\n--- Задание 2: checkSumSign ---");
        checkSumSign();

        System.out.println("\n--- Задание 3: printColor ---");
        printColor();

        System.out.println("\n--- Задание 4: compareNumbers ---");
        compareNumbers();

        System.out.println("\n--- Задание 5: checkSumRange (10-20) ---");
        System.out.println("Сумма 5 и 10: " + checkSumRange(5, 10)); // true
        System.out.println("Сумма 1 и 5: " + checkSumRange(1, 5));  // false

        System.out.println("\n--- Задание 6: printPositiveNegative ---");
        printPositiveNegative(5);
        printPositiveNegative(-3);
        printPositiveNegative(0);

        System.out.println("\n--- Задание 7: isNegative ---");
        System.out.println("Число -5 отрицательное? " + isNegative(-5));
        System.out.println("Число 3 отрицательное? " + isNegative(3));

        System.out.println("\n--- Задание 8: printStringNTimes ---");
        printStringNTimes("Привет, Java!", 3);

        System.out.println("\n--- Задание 9: isLeapYear ---");
        System.out.println("2020 високосный? " + isLeapYear(2020)); // true
        System.out.println("2100 високосный? " + isLeapYear(2100)); // false
        System.out.println("2000 високосный? " + isLeapYear(2000)); // true

        System.out.println("\n--- Задание 10: invertArray ---");
        int[] binArr = {1, 1, 0, 0, 1, 0, 1, 1, 0, 0};
        System.out.println("Было:  " + Arrays.toString(binArr));
        invertArray(binArr);
        System.out.println("Стало: " + Arrays.toString(binArr));

        System.out.println("\n--- Задание 11: fillArray100 ---");
        int[] filledArr = fillArray100();
        System.out.println("Первые 10 элементов: " + Arrays.toString(Arrays.copyOf(filledArr, 10)));

        System.out.println("\n--- Задание 12: multiplySmallNumbers ---");
        int[] numArr = {1, 5, 3, 2, 11, 4, 5, 2, 4, 8, 9, 1};
        System.out.println("Было:  " + Arrays.toString(numArr));
        multiplySmallNumbers(numArr);
        System.out.println("Стало: " + Arrays.toString(numArr));

        System.out.println("\n--- Задание 13: fillDiagonalOnes (матрица 5x5) ---");
        int[][] matrix = new int[5][5];
        fillDiagonalOnes(matrix);
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println("\n--- Задание 14: createArray ---");
        int[] createdArr = createArray(5, 7);
        System.out.println("Массив длиной 5, заполненный 7: " + Arrays.toString(createdArr));
    }

    // 1. Метод printThreeWords
    public static void printThreeWords() {
        System.out.println("Orange");
        System.out.println("Banana");
        System.out.println("Apple");
    }

    // 2. Метод checkSumSign
    public static void checkSumSign() {
        int a = 5;
        int b = -2;
        if (a + b >= 0) {
            System.out.println("Сумма положительная");
        } else {
            System.out.println("Сумма отрицательная");
        }
    }


    // 3. Метод printColor
    public static void printColor() {
        int value = 50;
        if (value <= 0) {
            System.out.println("Красный");
        } else if (value > 0 && value <= 100) {
            System.out.println("Желтый");
        } else {
            System.out.println("Зеленый");
        }
    }

    // 4. Метод compareNumbers
    public static void compareNumbers() {
        int a = 10;
        int b = 8;
        if (a >= b) {
            System.out.println("a >= b");
        } else {
            System.out.println("a < b");
        }
    }

    // 5. Метод проверки суммы в диапазоне 10-20
    public static boolean checkSumRange(int a, int b) {
        return a + b >= 10 && a + b <= 20;
    }

    // 6. Метод печати положительного/отрицательного (0 считаем положительным)
    public static void printPositiveNegative(int number) {
        if (number >= 0) {
            System.out.println(number + " -> положительное");
        } else {
            System.out.println(number + " -> отрицательное");
        }
    }

    // 7. Метод возврата true, если число отрицательное (0 положительное)
    public static boolean isNegative(int number) {
        return number < 0;
    }

    // 8. Метод печати строки N раз
    public static void printStringNTimes(String str, int n) {
        for (int i = 0; i < n; i++) {
            System.out.println(str);
        }
    }

    // 9. Метод проверки високосного года
    public static boolean isLeapYear(int year) {
        return (year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);
    }

    // 10. Метод инверсии 0 и 1 в массиве
    public static void invertArray(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (arr[i] == 0) ? 1 : 0;  // упрощенная запись
        }
    }

    // 11. Создание массива 100 и заполнение числами 1-100
    public static int[] fillArray100() {
        int[] arr = new int[100];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + 1;
        }
        return arr;
    }

    // 12. Умножение чисел меньше 6 на 2
    public static void multiplySmallNumbers(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < 6) {
                arr[i] *= 2;
            }
        }
    }

    // 13. Заполнение диагоналей квадратного массива единицами
    public static void fillDiagonalOnes(int[][] matrix) {
        int size = matrix.length;
        for (int i = 0; i < size; i++) {
            matrix[i][i] = 1;
            matrix[i][size - 1 - i] = 1;
        }
    }

    // 14. Метод создания массива с заданным значением
    public static int[] createArray(int len, int initialValue) {
        int[] arr = new int[len];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = initialValue;
        }
        return arr;
    }
}
