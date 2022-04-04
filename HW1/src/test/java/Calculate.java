public class Calculate {
    int justTest;
    Calculate cal;

    public Calculate(int justTest, Calculate cal) {
        this.justTest = justTest;
        this.cal = cal;
    }

    public Calculate(int justTest) {
        this.justTest = justTest;
    }

    public int getJustTest() {
        return justTest;
    }

    public void setJustTest(int justTest) {
        this.justTest = justTest;
    }

    public Calculate getCal() {
        return cal;
    }

    public void setCal(Calculate cal) {
        this.cal = cal;
    }

    int add(int a, int b) {
        return a+b;
    }

    int sub(int a, int b) {
        return a-b;
    }

    public static void main(String[] args) {
        Calculate calculate1 = new Calculate(10);
        Calculate calculate2 = new Calculate(20, calculate1);
        calculate2.getCal().setJustTest(30);
        System.out.println(calculate1.getJustTest());
    }
}
