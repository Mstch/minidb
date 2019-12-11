package com.minidb.consensus.raft.model;

import com.minidb.common.model.Resp;

public class LogResp extends Resp {

    Integer term;
    Boolean success;

    public LogResp(Integer term, Boolean success) {
        this.term = term;
        this.success = success;
    }
}
