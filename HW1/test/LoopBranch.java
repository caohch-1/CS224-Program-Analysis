class LoopBranch{
    void main(int c, boolean d) {
        int x, y, z=0;
        x = 1;
        y = 2;
        if (c > 0) {
            do {
                x = y + 1;
                y = 2 * z;
                if (d) {
                    x = y + z;
                }
                z = 1;
            } while (c < 20);
        }
        z = x;
    }

}

