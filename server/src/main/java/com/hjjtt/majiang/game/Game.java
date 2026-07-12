package com.hjjtt.majiang.game;

import com.hjjtt.majiang.message.Message;
import com.hjjtt.majiang.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    /** 每个座位是否已声明立直。 */
    private final boolean[] riichi = new boolean[4];
    /** 每个座位最近一次摸到的牌（立直后只能出这张）。 */
    private final String[] drawnTile = new String[4];
    /** 每个座位的弃牌（用于振听判定）。 */
    private final List<String>[] discards = new List[4];
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
        for (int i = 0; i < 4; i++) { hands[i] = new ArrayList<>(); discards[i] = new ArrayList<>(); }
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
        drawnTile[seat] = tile;
        hands[seat].add(tile);
        Collections.sort(hands[seat]);
        seats[seat].send(Message.reply(0, MessageType.DRAW_TILE, Map.of("tile", tile)));
        if (seats[seat].isAi())
            scheduler.schedule(() -> aiPlay(seat), 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** 出牌（下块接仲裁）。 */
    public synchronized void discard(int seat, String tile) {
        if (phase != Phase.PLAYING || seat != currentSeat) return;
        // 立直后只能打出刚摸到的牌
        if (riichi[seat] && drawnTile[seat] != null && !drawnTile[seat].equals(tile)) {
            seats[seat].send(Message.reply(0, MessageType.ERROR,
                    Map.of("code", "RIICHI_LOCK", "message", "立直后只能打刚摸到的牌")));
            return;
        }
        if (!hands[seat].remove(tile)) return;
        discards[seat].add(tile);
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
        if (rule.canHu(new ArrayList<>(h), null) && rule.hasYaku(new ArrayList<>(h), riichi[seat], true, true)) { settleWin(seat, true); return; }
        discard(seat, aiPick(seat));
    }

    /** AI 选牌：随机打出一张。 */
    private String aiPick(int seat) {
        List<String> h = hands[seat];
        return h.get(new java.util.Random().nextInt(h.size()));
    }

    /**
     * 玩家声明动作：
     *   riichi - 自己回合声明立直（PLAYING 阶段）
     *   hu     - 荣和点炮牌（WAITING_ACTION 阶段）
     * 碰/杠/过暂未实现，靠超时流转。
     */
    public synchronized void claim(int seat, String action, List<String> tiles) {
        if ("riichi".equalsIgnoreCase(action)) {
            if (phase != Phase.PLAYING || seat != currentSeat) return;
            riichi[seat] = true;
            seats[seat].send(Message.reply(0, MessageType.CLAIM_RESULT,
                    Map.of("ok", true, "action", "riichi", "seat", seat)));
            return;
        }
        if ("hu".equalsIgnoreCase(action)) {
            // 自摸：自己回合摸牌后声明胡
            if (phase == Phase.PLAYING && seat == currentSeat) {
                List<String> hm = new ArrayList<>(hands[seat]);
                if (rule.canHu(hm, null) && rule.hasYaku(hm, riichi[seat], true, true)) settleWin(seat, true);
                return;
            }
            // 荣和：点炮牌
            if (phase != Phase.WAITING_ACTION) return;
            if (discards[seat].contains(lastDiscardTile)) {
                seats[seat].send(Message.reply(0, MessageType.ERROR,
                        Map.of("code", "FURITEN", "message", "振听：不能荣和曾弃过的牌")));
                return;
            }
            List<String> h = new ArrayList<>(hands[seat]);
            h.add(lastDiscardTile);
            if (rule.canHu(h, null) && rule.hasYaku(h, riichi[seat], false, true)) settleWin(seat, false);
        }
    }

    /** 超时仲裁：无胡则下家摸牌。 */
    private synchronized void resolveAction() {
        if (phase != Phase.WAITING_ACTION) return;
        phase = Phase.PLAYING;
        nextTurn();
    }

    /** 胡牌结算：算分并广播。isSelfDraw=true 自摸，false 荣和。 */
    private void settleWin(int winner, boolean isSelfDraw) {
        List<String> winHand = new ArrayList<>(hands[winner]);
        if (!isSelfDraw) winHand.add(lastDiscardTile);
        Score sc = rule.score(winHand, riichi[winner], isSelfDraw, true,
                winner == dealer, !isSelfDraw);
        phase = Phase.SETTLED;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("winner", winner);
        data.put("selfDraw", isSelfDraw);
        if (sc != null) {
            data.put("han", sc.han());
            data.put("fu", sc.fu());
            data.put("points", sc.totalPoints());
            data.put("yaku", sc.yaku());
        }
        for (int s = 0; s < 4; s++)
            seats[s].send(Message.reply(0, MessageType.GAME_OVER, data));
        log.info("[{}] 胡牌结算 winner={} han={} points={}", roomId, winner,
                sc == null ? 0 : sc.han(), sc == null ? 0 : sc.totalPoints());
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
