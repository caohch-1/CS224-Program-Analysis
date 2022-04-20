public class Exception extends Throwable {
    public static void main(String[] args) {
        int data = 1;
        int a = 2;
        int b = 3;
        int c = 4;
        try {
            if (data > 0 ){
                System.out.println("right?");
                a = b - a;
                c = a + b + c;
            }else{
                b = a - 2;
                c = a;
                throw new Exception();
            }
        }catch(Exception e){
            if(data == 1){
                a += 3;
                b = c - a;
                System.out.println("data = 1,in the exception!!");

            }else{
                b -= 4;
                a = 5;
                System.out.println("data != 1,in the exception!!");
            }
        }
        a = b;
        c = a + 5;
    }
}
