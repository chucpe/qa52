package lesson2_5;

class MyArraySizeException extends Exception {
    public MyArraySizeException(String message) {
        super(message);
    }
}

class MyArrayDataException extends Exception {
    public MyArrayDataException(String message) {
        super(message);
    }
}

public class Main {

    // Основной метод для обработки массива
    public static int processArray(String[][] array) throws MyArraySizeException, MyArrayDataException {
        // Проверка размера массива
        if (array.length != 4) {
            throw new MyArraySizeException("Массив должен иметь размер 4x4. Получено строк: " + array.length);
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i].length != 4) {
                throw new MyArraySizeException("Массив должен иметь размер 4x4. Строка " + i + " имеет длину " + array[i].length);
            }
        }

        int sum = 0;

        // Проход по всем элементам и суммирование
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                try {
                    sum += Integer.parseInt(array[i][j]);
                } catch (NumberFormatException e) {
                    throw new MyArrayDataException(
                            "Неверные данные в ячейке [" + i + "][" + j + "]: '" + array[i][j] + "'"
                    );
                }
            }
        }

        return sum;
    }

    // Метод для демонстрации ArrayIndexOutOfBoundsException
    public static void demonstrateArrayIndexOutOfBounds() {
        try {
            String[][] array = new String[4][4];
            // Попытка обратиться к несуществующему индексу
            String value = array[5][3]; // Бросит ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Поймано ArrayIndexOutOfBoundsException: " + e.getMessage());
            System.out.println("Детали: попытка доступа к индексу за пределами массива");
        }
    }

    public static void main(String[] args) {
        // Тест 1: Корректный массив
        System.out.println("=== Тест 1: Корректный массив ===");
        String[][] validArray = {
                {"1", "2", "3", "4"},
                {"5", "6", "7", "8"},
                {"9", "10", "11", "12"},
                {"13", "14", "15", "16"}
        };

        try {
            int result = processArray(validArray);
            System.out.println("Сумма элементов: " + result);
        } catch (MyArraySizeException | MyArrayDataException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тест 2: Неверный размер массива ===");
        String[][] wrongSizeArray = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"}
        };

        try {
            int result = processArray(wrongSizeArray);
            System.out.println("Сумма элементов: " + result);
        } catch (MyArraySizeException | MyArrayDataException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тест 3: Неверные данные в ячейке ===");
        String[][] invalidDataArray = {
                {"1", "2", "3", "4"},
                {"5", "6", "7", "восемь"},
                {"9", "10", "11", "12"},
                {"13", "14", "15", "16"}
        };

        try {
            int result = processArray(invalidDataArray);
            System.out.println("Сумма элементов: " + result);
        } catch (MyArraySizeException | MyArrayDataException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тест 4: Демонстрация ArrayIndexOutOfBoundsException ===");
        demonstrateArrayIndexOutOfBounds();

        // Дополнительный пример с генерацией и поимкой ArrayIndexOutOfBoundsException
        System.out.println("\n=== Тест 5: Еще один пример с ArrayIndexOutOfBoundsException ===");
        try {
            String[][] anotherArray = new String[2][2];
            anotherArray[0][0] = "test";
            // Выход за границы массива
            String value = anotherArray[1][0]; // Это нормально
            value = anotherArray[2][0]; // Это бросит исключение
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Поймано ArrayIndexOutOfBoundsException!");
            System.out.println("Сообщение: " + e.getMessage());
            System.out.println("Класс исключения: " + e.getClass().getSimpleName());

            // Детализация исключения
            StackTraceElement[] stackTrace = e.getStackTrace();
            System.out.println("Стек вызовов:");
            for (StackTraceElement element : stackTrace) {
                System.out.println("  " + element);
            }
        }
    }
}