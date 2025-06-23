package com.thrillpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ExampleThreadDump {
    public static void main( String[] args ) throws InterruptedException {

        ThreadFactory namedThreadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("swanky-pool-" + count.getAndIncrement());
                return thread;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(4, namedThreadFactory);

        for (int i = 0; i < 4; i++) {
            executor.submit(() -> System.out.println("hi from " + Thread.currentThread()));
        }

        for (int i = 0; i < 100000; i++) {
            takesOne();
            takesTwo();
            takesThree();
        }
    }

    public static void takesOne() throws InterruptedException {
        Thread.sleep(100);
    }

    public static void takesTwo() throws InterruptedException {
        Thread.sleep(200);
    }

    public static void takesThree() throws InterruptedException {
        Thread.sleep(300);
    }
}
