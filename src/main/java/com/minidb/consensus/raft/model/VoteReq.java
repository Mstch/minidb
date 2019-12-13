package com.minidb.consensus.raft.model;

public class VoteReq extends Req {
    public Integer term;
    public Integer candidateId;
    public Integer lastLogIndex;
    public Integer lastLogTerm;

    public VoteReq(Integer term, Integer candidateId, Integer lastLogIndex, Integer lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String toString() {
        return "VoteReq{" +
                "term=" + term +
                ", candidateId=" + candidateId +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                '}';
    }
}
