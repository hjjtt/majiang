package com.hjjtt.majiang.game;

import com.hjjtt.majiang.message.Message;

/**
 * 玩家抽象。真人（WebSocket）与 AI（进程内）共同实现此接口，
 * 使牌桌逻辑对人与 AI 透明——人机对战与联网对战复用同一套 Game。
 */
public interface Player {

    /** 玩家唯一 id */
    String getId();

    /** 显示名 */
    String getName();

    /** 是否 AI */
    boolean isAi();

    /** 座位号（0..3），由 Game 在入座时分配 */
    void setSeat(int seat);

    int getSeat();

    /**
     * 推送一条消息给该玩家。
     * 真人实现通过 WebSocket 下发；AI 实现可忽略或用于驱动决策。
     */
    void send(Message msg);
}
