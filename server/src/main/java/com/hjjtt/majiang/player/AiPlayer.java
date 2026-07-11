package com.hjjtt.majiang.player;

import com.hjjtt.majiang.game.Player;
import com.hjjtt.majiang.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI 玩家（进程内 Bot）。作为实现了 Player 的 Bot 填入空座位，
 * 对 Game 而言与真人接口一致。决策逻辑待实现。
 */
public class AiPlayer implements Player {
    private static final Logger log = LoggerFactory.getLogger(AiPlayer.class);

    private final String id;
    private final String name;
    private int seat = -1;

    public AiPlayer(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public boolean isAi() { return true; }
    @Override public void setSeat(int seat) { this.seat = seat; }
    @Override public int getSeat() { return seat; }

    @Override
    public void send(Message msg) {
        // AI 收到服务端推送后的决策钩子。
        // TODO: 接入决策逻辑（出牌 / 声明动作）。
        log.debug("AI {} recv: {}", id, msg.getType());
    }
}
