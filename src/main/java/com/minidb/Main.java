package com.minidb;

import com.minidb.consensus.Election;

public class Main {

    public static void main(String[] args) {
        Election election = Election.instance;
        election.start();
    }
}
