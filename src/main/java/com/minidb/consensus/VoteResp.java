package com.minidb.consensus;

public class VoteResp {
    int term;
    boolean voteGranted;

    public VoteResp(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}
