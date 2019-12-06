package com.minidb.consensus.model;

import com.minidb.common.model.Resp;
import lombok.ToString;

@ToString
public class VoteResp  extends Resp {
   public int term;
    public boolean voteGranted;

    public VoteResp(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}
