public class Func {
    int add(int a, int b) {
        int c = a + b;
        return c;
    }

    public static void main(String[] args) {	[]
        Func func = new Func();	[func]
        int a = 10;	[func, a]
        int b = 20;	[func, a, b]
        int c = func.add(a, b);	[c]
        System.out.println(c);	[]
    }
}
