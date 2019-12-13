package com.minidb.consensus.raft;

import com.minidb.consensus.raft.model.AppendResp;
import com.minidb.consensus.raft.model.Node;
import com.minidb.consensus.raft.model.Log;

public class RaftConsensusImpl implements Raft {
    private Node node = Node.instance;
    private ConsensusClient client;

    @Override
    public AppendResp appendLog(Log log) {
        //本地写入tmp logs
        node.appendLog(log);
        //发送并清空tmp log(心跳时触发)
        //多数返回响应客户端
        return new AppendResp();
    }
}
