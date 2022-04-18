public class Func {
    int add(int a, int b) {
        int c = a + b;
        return c;
    }

    public static void main(String[] args) {
        Func func = new Func();
        int a = 10;
        int b = 20;
        int c = func.add(a, b);
        System.out.println(c);
    }
}
