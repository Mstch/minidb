package com.minidb;

import com.minidb.consensus.ConsensusClient;
import com.minidb.consensus.ConsensusServer;

import java.util.concurrent.ExecutionException;

public class Main {

    static Integer a = 0;

    public static void main(String[] args) throws InterruptedException {

            new ConsensusServer().start();
            new ConsensusClient().start();

    }
}
