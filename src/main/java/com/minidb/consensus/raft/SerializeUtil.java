package com.minidb.consensus.raft;

import com.google.gson.Gson;
import io.netty.util.CharsetUtil;

public class SerializeUtil {

    private static Gson gson = new Gson();

    public static String serializeToJson(Object src) {
        return gson.toJson(src);
    }

    public static byte[] serializeToByte(Object src) {
        //TODO 找到更快的序列化方案
        return gson.toJson(src).getBytes(CharsetUtil.UTF_8);
    }

    public static Object deserializeFromByte(byte[] src) {
        //TODO 找到更快的序列化方案
        return gson.toJson(src).getBytes(CharsetUtil.UTF_8);
    }
}
