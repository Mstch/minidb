package com.minidb.consensus.raft;

import com.minidb.consensus.raft.model.AppendResp;
import com.minidb.consensus.raft.model.Log;

public interface Raft {
   AppendResp appendLog(Log log);
}
