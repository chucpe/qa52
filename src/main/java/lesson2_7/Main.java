package lesson2_7;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            showMainMenu();
            int choice = getChoice();

            if (choice == 0) {
                System.out.println("Программа завершена. До свидания!");
                break;
            }

            switch (choice) {
                case 1:
                    runFactorialCalculator();
                    break;
                case 2:
                    runTriangleAreaCalculator();
                    break;
                case 3:
                    runArithmeticCalculator();
                    break;
                case 4:
                    runNumberComparator();
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }

            System.out.println("\nНажмите Enter для продолжения...");
            scanner.nextLine();
            scanner.nextLine(); // Очистка буфера
        }
        scanner.close();
    }

    private static void showMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("         ГЛАВНОЕ МЕНЮ ПРОГРАММ");
        System.out.println("=".repeat(50));
        System.out.println("1. Вычисление факториала числа");
        System.out.println("2. Вычисление площади треугольника");
        System.out.println("3. Арифметические операции с двумя числами");
        System.out.println("4. Сравнение двух чисел");
        System.out.println("0. Выход");
        System.out.println("=".repeat(50));
        System.out.print("Выберите программу (0-4): ");
    }

    private static int getChoice() {
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера
            return choice;
        } catch (Exception e) {
            scanner.nextLine(); // Очистка буфера при ошибке
            return -1;
        }
    }

    // ===== ПРОГРАММА 1: ФАКТОРИАЛ =====
    private static void runFactorialCalculator() {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("ВЫЧИСЛЕНИЕ ФАКТОРИАЛА");
        System.out.println("-".repeat(40));

        try {
            System.out.print("Введите число для вычисления факториала: ");
            int n = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера

            long result = FactorialCalculator.calculateFactorial(n);
            System.out.printf("Факториал числа %d! = %d%n", n, result);

        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка ввода: введите целое число");
            scanner.nextLine(); // Очистка буфера
        }
    }

    // ===== ПРОГРАММА 2: ПЛОЩАДЬ ТРЕУГОЛЬНИКА =====
    private static void runTriangleAreaCalculator() {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("ВЫЧИСЛЕНИЕ ПЛОЩАДИ ТРЕУГОЛЬНИКА");
        System.out.println("-".repeat(40));

        try {
            System.out.println("Выберите способ вычисления:");
            System.out.println("  1 - через основание и высоту");
            System.out.println("  2 - по формуле Герона (по трем сторонам)");
            System.out.print("Ваш выбор (1 или 2): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Очистка буфера

            if (choice == 1) {
                System.out.print("Введите основание: ");
                double base = scanner.nextDouble();
                scanner.nextLine();
                System.out.print("Введите высоту: ");
                double height = scanner.nextDouble();
                scanner.nextLine();

                double area = TriangleAreaCalculator.calculateArea(base, height);
                System.out.printf("Площадь треугольника = %.2f%n", area);

            } else if (choice == 2) {
                System.out.print("Введите сторону a: ");
                double a = scanner.nextDouble();
                scanner.nextLine();
                System.out.print("Введите сторону b: ");
                double b = scanner.nextDouble();
                scanner.nextLine();
                System.out.print("Введите сторону c: ");
                double c = scanner.nextDouble();
                scanner.nextLine();

                double area = TriangleAreaCalculator.calculateAreaByHeron(a, b, c);
                System.out.printf("Площадь треугольника = %.4f%n", area);

            } else {
                System.err.println("Неверный выбор. Попробуйте снова.");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка ввода: проверьте правильность введенных данных");
            scanner.nextLine(); // Очистка буфера
        }
    }

    // ===== ПРОГРАММА 3: АРИФМЕТИКА =====
    private static void runArithmeticCalculator() {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("АРИФМЕТИЧЕСКИЕ ОПЕРАЦИИ");
        System.out.println("-".repeat(40));

        try {
            System.out.print("Введите первое число: ");
            int a = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Введите второе число: ");
            int b = scanner.nextInt();
            scanner.nextLine();

            System.out.println("\nРезультаты операций:");
            System.out.printf("  %d + %d = %d%n", a, b, ArithmeticCalculator.add(a, b));
            System.out.printf("  %d - %d = %d%n", a, b, ArithmeticCalculator.subtract(a, b));
            System.out.printf("  %d * %d = %d%n", a, b, ArithmeticCalculator.multiply(a, b));

            try {
                System.out.printf("  %d / %d = %.2f%n", a, b, ArithmeticCalculator.divide(a, b));
            } catch (ArithmeticException e) {
                System.out.println("  " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Ошибка ввода: введите целые числа");
            scanner.nextLine(); // Очистка буфера
        }
    }

    // ===== ПРОГРАММА 4: СРАВНЕНИЕ =====
    private static void runNumberComparator() {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("СРАВНЕНИЕ ДВУХ ЧИСЕЛ");
        System.out.println("-".repeat(40));

        try {
            System.out.print("Введите первое число: ");
            int a = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Введите второе число: ");
            int b = scanner.nextInt();
            scanner.nextLine();

            System.out.println("\nРезультаты сравнения:");

            if (NumberComparator.isGreater(a, b)) {
                System.out.printf("  %d > %d (первое число больше)%n", a, b);
            } else if (NumberComparator.isLess(a, b)) {
                System.out.printf("  %d < %d (первое число меньше)%n", a, b);
            } else {
                System.out.printf("  %d = %d (числа равны)%n", a, b);
            }

            // Дополнительная информация
            int compareResult = NumberComparator.compare(a, b);
            System.out.println("\nДетальная информация:");
            System.out.printf("  compare(%d, %d) = %d%n", a, b, compareResult);

            if (compareResult > 0) {
                System.out.printf("  %d больше %d на %d%n", a, b, a - b);
            } else if (compareResult < 0) {
                System.out.printf("  %d меньше %d на %d%n", a, b, b - a);
            } else {
                System.out.println("  Числа равны");
            }

        } catch (Exception e) {
            System.err.println("Ошибка ввода: введите целые числа");
            scanner.nextLine(); // Очистка буфера
        }
    }
}
