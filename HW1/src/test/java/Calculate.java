public class Calculate {
    int add(int a, int b) {
        return a+b;
    }

    int sub(int a, int b) {
        return a-b;
    }

    public static void main(String[] args) {
        Calculate calculate = new Calculate();
        int var1 = 1;
        int var2 = 4;
        int var3 = calculate.add(var1, var2);
        System.out.println(calculate.sub(var3, var2));
    }
}
