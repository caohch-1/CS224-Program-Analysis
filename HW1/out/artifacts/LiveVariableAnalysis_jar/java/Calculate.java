public class Calculate {
    int anInt;

    public Calculate(int justTest) {
        this.anInt = justTest;
    }

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    int add(int a, int b) {
        return a+b;
    }

    int sub(int a, int b) {
        return a-b;
    }

    public static void main(String[] args) {
        Calculate calculate1 = new Calculate(10);
        for (int i = 0; i < 5; i ++) {
            calculate1.setAnInt(calculate1.add(calculate1.getAnInt(), 1));
        }
        System.out.println(calculate1.getAnInt());

        int i = 0;
        while(i < 5) {
            i ++;
            calculate1.setAnInt(calculate1.sub(calculate1.getAnInt(), 1));
        }
        System.out.println(calculate1.getAnInt());

        if (i == 5) {
            System.out.println(calculate1.getAnInt());
        } else {
            calculate1.setAnInt(calculate1.add(calculate1.getAnInt(), 1));
            calculate1.setAnInt(calculate1.sub(calculate1.getAnInt(), 1));
            System.out.println(calculate1.getAnInt());
        }
    }
}
