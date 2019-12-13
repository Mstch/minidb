package com.minidb.consensus.raft.model;

public class VoteResp  extends Resp {
   public int term;
    public boolean voteGranted;

    public VoteResp(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}
