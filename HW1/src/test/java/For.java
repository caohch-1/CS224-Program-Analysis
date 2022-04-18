import java.util.Random;

public class For {
    public static void main(String[] args) {
        int a = new Random().nextInt(100);
        for (int i = 0; i < a; i ++) {
            a += i;
        }
        System.out.println(a);
    }
}
