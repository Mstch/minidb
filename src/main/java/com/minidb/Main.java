package com.minidb;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    static Integer a = 0;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue queue = new ArrayBlockingQueue(10);
        for (int i = 0; i < 10; i++) {
            queue.put(i);
        }
        for (int i = 0; i < 5; i++) {
            queue.take();
        }
        queue.put(1);

    }
}
