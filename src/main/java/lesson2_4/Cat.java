package lesson2_4;

public class Cat extends Animal {
    private static int catCount = 0;
    private boolean isFull;

    public Cat(String name) {
        super(name, 200, 0); // коты не умеют плавать
        this.isFull = false; // по умолчанию голодны
        catCount++;
    }

    public void eat(Bowl bowl, int amount) {
        if (bowl.getFood() >= amount) {
            bowl.decreaseFood(amount);
            isFull = true;
            System.out.println(name + " поел(а) " + amount + " еды. Теперь сыт(а)!");
        } else {
            System.out.println(name + " не хватило еды в миске. Остался(ась) голодным(ой).");
        }
    }

    public boolean isFull() {
        return isFull;
    }

    public static int getCatCount() {
        return catCount;
    }
}