package com.hjjtt.majiang.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 一局牌桌：4 个座位（Player）+ 状态机骨架。
 * 规则逻辑通过 MahjongRule 注入；骨架阶段不实现具体牌局流程。
 */
public class Game {
    private static final Logger log = LoggerFactory.getLogger(Game.class);

    public enum Phase { WAITING, DEALING, PLAYING, FINISHED }

    private final String roomId;
    private final Player[] seats = new Player[4];
    private final MahjongRule rule;
    private Phase phase = Phase.WAITING;

    public Game(MahjongRule rule) {
        this.rule = rule;
        this.roomId = "r_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** 入座，返回分配到的座位号；已满返回 -1。 */
    public synchronized int sitDown(Player p) {
        for (int i = 0; i < 4; i++) {
            if (seats[i] == null) {
                seats[i] = p;
                p.setSeat(i);
                return i;
            }
        }
        return -1;
    }

    public boolean isFull() {
        for (Player s : seats) {
            if (s == null) return false;
        }
        return true;
    }

    /** 开局：发牌、定庄。TODO(规则阶段)：实现完整流程。 */
    public synchronized void start() {
        if (!isFull()) {
            log.warn("[{}] 座位未满，无法开局", roomId);
            return;
        }
        this.phase = Phase.DEALING;
        log.info("[{}] 牌局开始，规则={}", roomId, rule.getClass().getSimpleName());
        // TODO: 洗牌 → 发牌(每人 13，庄家 14) → 推送 dealTiles → 进入 PLAYING
    }

    public String getRoomId() { return roomId; }
    public Phase getPhase() { return phase; }
    public List<Player> players() { return Arrays.asList(seats); }
}
