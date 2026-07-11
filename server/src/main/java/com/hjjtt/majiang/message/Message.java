package com.hjjtt.majiang.message;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息信封 { type, seq, data }，对应 protocol/messages.md §1。
 * data 用扁平 Map 承载，骨架阶段足够；规则确定后可替换为强类型消息体。
 */
public class Message {

    private String type;
    private int seq;
    private Map<String, Object> data;

    public Message() {}

    public static Message reply(int seq, String type, Map<String, Object> data) {
        Message m = new Message();
        m.seq = seq;
        m.type = type;
        m.data = data != null ? new LinkedHashMap<>(data) : null;
        return m;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getSeq() { return seq; }
    public void setSeq(int seq) { this.seq = seq; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
