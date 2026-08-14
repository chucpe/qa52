package lesson2_4;

public class Bowl {
    private int food;

    public Bowl(int initialFood) {
        this.food = Math.max(initialFood, 0);
    }

    public int getFood() {
        return food;
    }

    public void addFood(int amount) {
        if (amount > 0) {
            food += amount;
            System.out.println("В миску добавлено " + amount + " еды. Теперь в миске " + food + " еды.");
        } else {
            System.out.println("Нельзя добавить отрицательное количество еды!");
        }
    }

    public void decreaseFood(int amount) {
        if (amount > 0 && amount <= food) {
            food -= amount;
        } else {
            System.out.println("Ошибка: недостаточно еды или некорректное количество!");
        }
    }

    public void printFoodInfo() {
        System.out.println("В миске " + food + " еды.");
    }
}