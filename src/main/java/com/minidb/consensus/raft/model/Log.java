package com.minidb.consensus.raft.model;


import com.minidb.common.KryoSerializer;

public interface Log {

    <T> T apply();

    Entries.Entry store();

    default byte[] serialize() {
        return KryoSerializer.instance.serialize(this);
    }


}

