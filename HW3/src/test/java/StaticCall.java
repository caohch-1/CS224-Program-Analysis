public class StaticCall {

    public static void main(String[] args) {
        foo();
        A1.baz();
    }

    static void foo() {
        bar();
    }

    static void bar() {
    }
}

class A1 {
    static void baz() {
        B1.qux();
    }
}

class B1 {
    static void qux() {
        A1.baz();
    }
}