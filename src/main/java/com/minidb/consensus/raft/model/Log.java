package com.minidb.consensus.raft.model;

public interface Log {

    <T> T apply();

    Entries.Entry store();
}
