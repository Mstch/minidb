package com.minidb.store;

import com.minidb.consensus.raft.model.Entries;
import com.minidb.consensus.raft.model.Log;

public class Page implements Log {

    int val = 1;
    @Override
    public <T> T apply() {

        return null;
    }

    @Override
    public Entries.Entry store() {
        return null;
    }
}
