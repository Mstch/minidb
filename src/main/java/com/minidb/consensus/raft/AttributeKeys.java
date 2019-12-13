package com.minidb.consensus.raft;

import io.netty.util.AttributeKey;

public class AttributeKeys {
    public static final AttributeKey<Integer> REMOTE_ID = AttributeKey.newInstance("REMOTE_ID");
}
