public class UnreachableIfBranch {
    public int main(String[] args) {
        int x = 10;
        int y = 1;
        int z;
        if (x > y + x / 2) {
            z = 100;
        } else {
            z = 200; // unreachable branch
            System.out.println("Hello"); // unreachable branch
            System.out.println("Hello"); // unreachable branch
        }
        return z;
    }
}
