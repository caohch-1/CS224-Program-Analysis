class BranchLoop {

    int main(int m, int n, int k) {
        int a, i;
        a=0;
        for (i = m - 1; i < k; i++) {
            if (i >= n) {
                a = n;
            }
            a = a + i;
        }
        return a;
    }

    
}
