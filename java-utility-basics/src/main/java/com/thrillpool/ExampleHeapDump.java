package com.thrillpool;


public class ExampleHeapDump
{
    public static void main( String[] args ) throws InterruptedException {
        String[] stringArr = new String[10000];
        SomeRandomClass[] someRandomClassArr = new SomeRandomClass[10000];

        for (int i = 0; i < 10000; i++) {
            stringArr[i] = "hi";
            someRandomClassArr[i] = new SomeRandomClass(1, 2);
        }

        Thread.sleep(10000000);

        for (int i = 0; i < 10000; i++) {
            System.out.println(stringArr[i]);
            System.out.println(someRandomClassArr[i]);
        }
    }
}

class SomeRandomClass {
    int x;
    int y;
    SomeRandomClass(int x, int y) {
        this.x = x;
        this.y = y;
    }
}