public class VirtualCall {

    public static void main(String[] args) {
        B2 b = new B2();
        b.foo();
    }
}

class A2 {
    void foo() {
    }
}

class B2 extends A2 {
}

class C extends B2 {
    void foo() {
    }
}

class D extends B2 {
    void foo() {
    }
}

class E extends A2 {
    void foo() {
    }
}
