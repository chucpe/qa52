package lesson2_3_1;

public class Main {
    public static void main(String[] args) {
        Product[] productsArray = new Product[6];

        productsArray[0] = new Product("Samsung S25 Ultra", "01.02.2025", "Samsung Corp.", "Korea", 5599.0, false);
        productsArray[1] = new Product("iPhone 16 Pro", "10.09.2024", "Apple Inc.", "USA", 89990.0, false);
        productsArray[2] = new Product("Xiaomi 16", "20.11.2024", "Xiaomi", "China", 49990.0, true);
        productsArray[3] = new Product("Huawei Pura 16", "15.03.2024", "Huawei", "China", 150.0, false);
        productsArray[4] = new Product("Apple iPhone 16 Pro", "10.09.2024", "Apple Inc.", "USA", 89990.0, false);
        productsArray[5] = new Product("Xiaomi 16", "20.11.2024", "Xiaomi", "China", 49990.0, true);

        System.out.println("--- ИНФОРМАЦИЯ О ТОВАРАХ ---");
        for (int i = 0; i < productsArray.length; i++) {
            productsArray[i].printInfo();
        }
    }
}