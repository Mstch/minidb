package com.minidb;


import com.minidb.store.Page;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static void main(String[] args) {
        Page page = new Page();
        System.out.println(new String(page.serialize()));

    }
}
