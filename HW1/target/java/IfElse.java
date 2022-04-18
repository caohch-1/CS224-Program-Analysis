import java.util.Random;

public class IfElse {
    public static void main(String[] args) {
        int a = new Random().nextInt(100) - 50;
        if (a < 0) {
            System.out.printf("Neg: %d\n", a);
        } else if (a > 0) {
            System.out.printf("Pos: %d\n", a);
        } else {
            System.out.printf("Zero: %d\n", a);
        }
    }
}
