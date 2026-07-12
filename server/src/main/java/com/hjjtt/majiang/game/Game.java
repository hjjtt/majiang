package com.hjjtt.majiang.game;

import com.hjjtt.majiang.message.Message;
import com.hjjtt.majiang.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 一局牌桌：四川血战。
 * 状态机：WAITING -> PLAYING -> WAITING_ACTION(出牌后仲裁) -> SETTLED。
 * 本块实现发牌 + 摸牌轮转；动作仲裁（出牌/碰杠胡仲裁）下块补。
 */
public class Game {
    private static final Logger log = LoggerFactory.getLogger(Game.class);

    public enum Phase { WAITING, PLAYING, WAITING_ACTION, SETTLED }

    private final String roomId;
    private final Player[] seats = new Player[4];
    private final MahjongRule rule;
    private Phase phase = Phase.WAITING;

    private Wall wall;
    private final List<String>[] hands = new List[4];
    private final String[] missingSuits = new String[4];
    private int currentSeat = 0;
    private int dealer = 0;
    private static final long ACTION_TIMEOUT_MS = 100L;
    private final java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
    private int lastDiscardSeat = -1;
    private String lastDiscardTile;

    public Game(MahjongRule rule) {
        this.rule = rule;
        this.roomId = "r_" + UUID.randomUUID().toString().substring(0, 8);
    }

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
        for (Player s : seats) if (s == null) return false;
        return true;
    }

    /** 开局：洗牌发牌，庄家 14 张先手。 */
    public synchronized void start() {
        if (!isFull()) { log.warn("[{}] 座位未满", roomId); return; }
        wall = new Wall();
        for (int i = 0; i < 4; i++) hands[i] = new ArrayList<>();
        for (int n = 0; n < 13; n++)
            for (int s = 0; s < 4; s++) hands[s].add(wall.draw());
        hands[dealer].add(wall.draw()); // 庄家先手
        currentSeat = dealer;
        phase = Phase.PLAYING;
        for (int s = 0; s < 4; s++) {
            Collections.sort(hands[s]);
            seats[s].send(Message.reply(0, MessageType.DEAL_TILES, Map.of("hand", hands[s])));
        }
        log.info("[{}] 开局 庄家={} 牌墙余={}", roomId, dealer, wall.remaining());
        seats[currentSeat].send(Message.reply(0, MessageType.TURN_CHANGE,
                Map.of("seat", currentSeat, "action", "discard")));
        if (seats[currentSeat].isAi())
            scheduler.schedule(() -> aiPlay(currentSeat), 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** 当前玩家摸一张牌。 */
    public synchronized void draw(int seat) {
        if (phase != Phase.PLAYING || seat != currentSeat) return;
        String tile = wall.draw();
        if (tile == null) { settle(); return; } // 流局
        hands[seat].add(tile);
        Collections.sort(hands[seat]);
        seats[seat].send(Message.reply(0, MessageType.DRAW_TILE, Map.of("tile", tile)));
        if (seats[seat].isAi())
            scheduler.schedule(() -> aiPlay(seat), 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** 出牌（下块接仲裁）。 */
    public synchronized void discard(int seat, String tile) {
        if (phase != Phase.PLAYING || seat != currentSeat) return;
        if (!hands[seat].remove(tile)) return;
        for (int s = 0; s < 4; s++)
            seats[s].send(Message.reply(0, MessageType.TILE_DISCARDED,
                    Map.of("seat", seat, "tile", tile)));
        phase = Phase.WAITING_ACTION;
        lastDiscardSeat = seat;
        lastDiscardTile = tile;
        scheduler.schedule(this::resolveAction, ACTION_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** 下家摸牌。 */
    private void nextTurn() {
        currentSeat = (currentSeat + 1) % 4;
        seats[currentSeat].send(Message.reply(0, MessageType.TURN_CHANGE,
                Map.of("seat", currentSeat, "action", "draw")));
        draw(currentSeat);
    }

    /** AI 行动：自摸胡检查，否则出牌。 */
    private synchronized void aiPlay(int seat) {
        if (phase != Phase.PLAYING || seat != currentSeat) return;
        List<String> h = hands[seat];
        if (h.isEmpty()) return;
        if (rule.canHu(new ArrayList<>(h), missingSuits[seat])) { settleWin(seat); return; }
        discard(seat, aiPick(seat));
    }

    /** AI 选牌：优先打缺门花色，否则随机。 */
    private String aiPick(int seat) {
        List<String> h = hands[seat];
        String miss = missingSuits[seat];
        if (miss != null) {
            for (String t : h) if (t.startsWith(miss)) return t;
        }
        return h.get(new java.util.Random().nextInt(h.size()));
    }

    /** 玩家声明动作（胡/碰/杠/过）。碰/杠骨架阶段暂未实现，靠超时流转。 */
    public synchronized void claim(int seat, String action, List<String> tiles) {
        if (phase != Phase.WAITING_ACTION) return;
        if ("hu".equalsIgnoreCase(action)) {
            List<String> h = new ArrayList<>(hands[seat]);
            h.add(lastDiscardTile);
            if (rule.canHu(h, missingSuits[seat])) settleWin(seat);
        }
    }

    /** 超时仲裁：无胡则下家摸牌。 */
    private synchronized void resolveAction() {
        if (phase != Phase.WAITING_ACTION) return;
        phase = Phase.PLAYING;
        nextTurn();
    }

    /** 胡牌结算。 */
    private void settleWin(int winner) {
        phase = Phase.SETTLED;
        for (int s = 0; s < 4; s++)
            seats[s].send(Message.reply(0, MessageType.GAME_OVER, Map.of("winner", winner)));
        log.info("[{}] 胡牌结算 winner={}", roomId, winner);
        scheduler.shutdown();
    }

    /** 流局结算。 */
    private void settle() {
        phase = Phase.SETTLED;
        for (int s = 0; s < 4; s++)
            seats[s].send(Message.reply(0, MessageType.GAME_OVER, Map.of("winner", -1)));
        log.info("[{}] 流局结算", roomId);
        scheduler.shutdown();
    }

    public String getRoomId() { return roomId; }
    public Phase getPhase() { return phase; }
    public List<Player> players() { return java.util.Arrays.asList(seats); }
}
