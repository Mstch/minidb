package com.minidb.consensus.raft.model;

import com.minidb.common.model.Req;
import com.minidb.store.Log;

public class LogReq extends Req {
    Integer term;
    Integer leaderId;
    Integer prevLogIndex;
    Integer prevLogTerm;
    Log[]   logs;
    Integer leaderCommit;

    public LogReq(Integer term, Integer leaderId, Integer prevLogIndex, Integer prevLogTerm, Log[] logs, Integer leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logs = logs;
        this.leaderCommit = leaderCommit;
    }
}
