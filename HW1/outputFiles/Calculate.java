Calculate calculate1 = new Calculate(10);	[calculate1]
for (int i = 0; i < 5; i ++) {	[calculate1]
    calculate1.setAnInt(calculate1.add(calculate1.getAnInt(), 1));	[i, calculate1]
}
System.out.println(calculate1.getAnInt());	[calculate1]

int i = 0;	[i, calculate1]
while(i < 5) {	[i, calculate1]
    i ++;	[i, calculate1]
    calculate1.setAnInt(calculate1.sub(calculate1.getAnInt(), 1));	[i, calculate1]
}
System.out.println(calculate1.getAnInt());	[i, calculate1]

if (i == 5) {	[calculate1]
    System.out.println(calculate1.getAnInt());	[]
} else {
    calculate1.setAnInt(calculate1.add(calculate1.getAnInt(), 1));	[calculate1]
    calculate1.setAnInt(calculate1.sub(calculate1.getAnInt(), 1));	[calculate1]
    System.out.println(calculate1.getAnInt());	[]
}
