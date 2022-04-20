int data = 1;	[data]
int a = 2;	[a, data]
int b = 3;	[b, a, data]
int c = 4;	[b, a, c, data]
try {	[b, a, c, data]
    if (data > 0 ){	[b, a, c, data]
        System.out.println("right?");	[b, a, c, data]
        a = b - a;	[b, a, c, data]
        c = a + b + c;	[b, a, c, data]
    }else{
        b = a - 2;	[b, a, c, data]
        c = a;	[b, a, c, data]
        throw new Exception();	[b, a, c, data]
    }
}catch(Exception e){	[b, a, c, data]
    if(data == 1){	[b, a, c]
        a += 3;	[a, c]
        b = c - a;	[b]
        System.out.println("data = 1,in the exception!!");	[b]

    }else{
        b -= 4;	[b]
        a = 5;	[b]
        System.out.println("data != 1,in the exception!!");	[b]
    }
}
a = b;	[a]
c = a + 5;	[]
