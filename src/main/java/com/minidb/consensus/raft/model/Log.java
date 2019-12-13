package com.minidb.consensus.raft.model;


import com.minidb.consensus.raft.SerializeUtil;
import io.netty.buffer.ByteBufUtil;

public interface Log{

    <T> T apply();

    Entries.Entry store();

    default byte[] serialize(){
        return SerializeUtil.serializeToByte(this);
    }

   default Log deserialize(byte[] bytes){

    };
}
