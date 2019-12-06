package com.minidb.consensus.model;

import com.minidb.common.model.Req;
import com.minidb.store.Log;

public class AppendReq extends Req {
    Integer term;
    Integer leaderId;
    Integer prevLogIndex;
    Integer prevLogTerm;
    Log[]   logs;

    public AppendReq(Integer term, Integer leaderId, Integer prevLogIndex, Integer prevLogTerm, Log[] logs) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logs = logs;
    }
}
