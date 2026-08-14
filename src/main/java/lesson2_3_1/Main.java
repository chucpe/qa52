package lesson2_3_1;

public class Main {
    public static void main(String[] args) {
        // 2. Создаем массив из 5 товаров
        Product[] productsArray = new Product[5];

        // Заполняем каждую ячейку массива объектами
        productsArray[0] = new Product("Samsung S25 Ultra", "01.02.2025", "Samsung Corp.", "Korea", 5599.0, true);
        productsArray[1] = new Product("iPhone 16 Pro", "10.09.2024", "Apple Inc.", "USA", 89990.0, false);
        productsArray[2] = new Product("Xiaomi 14", "20.11.2024", "Xiaomi", "China", 49990.0, true);
        productsArray[3] = new Product("Шоколад 'Алёнка'", "15.03.2026", "Красный Октябрь", "Россия", 150.0, false);
        productsArray[4] = new Product("Пылесос Bosch", "05.07.2025", "BOSCH", "Germany", 24990.0, false);

        System.out.println("--- ИНФОРМАЦИЯ О ТОВАРАХ ---");
        // Проходим циклом по массиву и вызываем метод printInfo()
        for (int i = 0; i < productsArray.length; i++) {
            productsArray[i].printInfo();
        }
    }
}