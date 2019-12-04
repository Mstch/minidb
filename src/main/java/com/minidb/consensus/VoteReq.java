package com.minidb.consensus;

public class VoteReq {
    Integer term;
    Integer candidateId;
    Integer lastLogIndex;
    Integer lastLogTerm;

    public VoteReq(Integer term, Integer candidateId, Integer lastLogIndex, Integer lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
}
