package com.minidb.consensus.raft.model;

public class LogReq extends Req {
    Integer term;
    Integer leaderId;
    Integer prevLogIndex;
    Integer prevLogTerm;
    Entries.Entry[]   logs;
    Integer leaderCommit;

    public LogReq(Integer term, Integer leaderId, Integer prevLogIndex, Integer prevLogTerm, Entries.Entry[] logs, Integer leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logs = logs;
        this.leaderCommit = leaderCommit;
    }
}
