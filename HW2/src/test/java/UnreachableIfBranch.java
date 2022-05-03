class UnreachableIfBranch {

    int main() {
        int x = 10;
        int y = 1;
        int z;
        if (x > y) {
            z = 100;
        } else {
            z = 200; // unreachable branch
        }

        if (x == 10) {
            z = 300;
        } else {
            z = 400; // unreachable branch
        }
        return z;
    }
}
