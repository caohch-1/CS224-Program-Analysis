class DeadAssignment {

    void main() {
        int x = 1;
        int y = x + 2; // dead assignment
        int z = x + 3;
        use(z);
        int a = x; // dead assignment
        int b = invoke();
    }

    void use(int n) {
    }

    int invoke() {
        System.out.println("Hello");
        return 0;
    }
}
