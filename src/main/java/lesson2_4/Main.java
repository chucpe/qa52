package lesson2_4;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Часть 1: Животные и их действия ===\n");

        // Создаем животных
        Dog bobik = new Dog("Бобик");
        Dog rex = new Dog("Рекс");
        Cat murka = new Cat("Мурка");
        Cat barsik = new Cat("Барсик");
        Cat vasya = new Cat("Вася");

        // Демонстрация действий животных
        System.out.println("--- Действия животных ---");
        bobik.run(150);
        bobik.swim(5);
        rex.run(600);
        rex.swim(15);

        murka.run(150);
        murka.swim(5); // кот не умеет плавать

        barsik.run(250); // не может пробежать больше 200 м
        System.out.println();

        // Подсчет животных
        System.out.println("--- Статистика ---");
        System.out.println("Всего животных: " + Animal.getAnimalCount());
        System.out.println("Всего собак: " + Dog.getDogCount());
        System.out.println("Всего котов: " + Cat.getCatCount());
        System.out.println();

        System.out.println("=== Часть 2: Коты и миска ===\n");

        // Создаем миску с едой
        Bowl bowl = new Bowl(15);
        bowl.printFoodInfo();
        System.out.println();

        // Создаем массив котов
        Cat[] cats = {
                new Cat("Мурка"),
                new Cat("Барсик"),
                new Cat("Вася"),
                new Cat("Снежок"),
                new Cat("Рыжик")
        };

        // Все коты пытаются покушать
        System.out.println("--- Коты кушают ---");
        for (Cat cat : cats) {
            cat.eat(bowl, 5); // каждый кот пытается съесть по 5 еды
            bowl.printFoodInfo();
        }

        // Выводим информацию о сытости
        System.out.println("\n--- Информация о сытости котов ---");
        for (Cat cat : cats) {
            System.out.println(cat.name + " сыт(а)? " + (cat.isFull() ? "Да" : "Нет"));
        }

        // Добавляем еду в миску
        System.out.println("\n--- Добавление еды ---");
        bowl.addFood(10);
        bowl.printFoodInfo();

        // Голодные коты пытаются поесть снова
        System.out.println("\n--- Голодные коты пробуют снова ---");
        for (Cat cat : cats) {
            if (!cat.isFull()) {
                cat.eat(bowl, 5);
                bowl.printFoodInfo();
            }
        }

        // Финальная информация о сытости
        System.out.println("\n--- Финальная информация о сытости котов ---");
        for (Cat cat : cats) {
            System.out.println(cat.name + " сыт(а)? " + (cat.isFull() ? "Да ✓" : "Нет ✗"));
        }
        bowl.printFoodInfo();
    }
}