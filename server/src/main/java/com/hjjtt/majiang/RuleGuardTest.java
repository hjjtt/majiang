package com.hjjtt.majiang;

import com.hjjtt.majiang.game.Game;
import com.hjjtt.majiang.game.Player;
import com.hjjtt.majiang.game.RiichiRule;
import com.hjjtt.majiang.message.Message;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 规则加固单测：立直强制出牌(RIICHI_LOCK) + 振听(FURITEN)。
 * 反射构造非法场景，断言收到对应 ERROR。
 */
public class RuleGuardTest {
    static int pass = 0, fail = 0;

    public static void main(String[] args) throws Exception {
        testRiichiLock();
        testFuriten();
        System.out.println("\n==== " + pass + " pass, " + fail + " fail ====");
        if (fail > 0) System.exit(1);
    }

    static void testRiichiLock() throws Exception {
        Game g = newGame();
        setArr(g, "riichi", 0, true);
        setArr(g, "drawnTile", 0, "D7");
        setField(g, "phase", Game.Phase.PLAYING);
        setField(g, "currentSeat", 0);
        setArr(g, "hands", 0, new ArrayList<>(List.of("W1", "W2", "W3")));
        CapturePlayer p0 = player(g, 0);
        g.discard(0, "W1"); // 立直后出非刚摸牌(D7) -> 应被拒
        chk("riichiLock.error", code(p0), "RIICHI_LOCK");
    }

    static void testFuriten() throws Exception {
        Game g = newGame();
        setArr(g, "discards", 0, new ArrayList<>(List.of("D7"))); // 自己弃过 D7
        setField(g, "lastDiscardTile", "D7");
        setField(g, "phase", Game.Phase.WAITING_ACTION);
        setArr(g, "hands", 0, new ArrayList<>(List.of(
                "W2","W2","W2","W3","W4","W5","T5","T6","T7","D4","D5","D6","D7")));
        CapturePlayer p0 = player(g, 0);
        g.claim(0, "hu", null); // 荣和 D7 但 D7 在自己弃牌 -> 振听
        chk("furiten.error", code(p0), "FURITEN");
    }

    // ---- helpers ----
    static Game newGame() throws Exception {
        Game g = new Game(new RiichiRule());
        for (int i = 0; i < 4; i++) g.sitDown(new CapturePlayer());
        return g;
    }
    static CapturePlayer player(Game g, int seat) throws Exception {
        Field f = Game.class.getDeclaredField("seats");
        f.setAccessible(true);
        Player[] seats = (Player[]) f.get(g);
        return (CapturePlayer) seats[seat];
    }
    static String code(CapturePlayer p) {
        if (p.last == null || !"error".equals(p.last.getType())) return null;
        Object c = p.last.getData().get("code");
        return c == null ? null : String.valueOf(c);
    }
    static void setField(Object obj, String name, Object val) throws Exception {
        Field f = Game.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, val);
    }
    @SuppressWarnings("unchecked")
    static void setArr(Object obj, String name, int idx, Object val) throws Exception {
        Field f = Game.class.getDeclaredField(name);
        f.setAccessible(true);
        Object arr = f.get(obj);
        if (arr instanceof boolean[]) ((boolean[]) arr)[idx] = (Boolean) val;
        else if (arr instanceof String[]) ((String[]) arr)[idx] = (String) val;
        else ((Object[]) arr)[idx] = val;
    }
    static void chk(String name, Object actual, Object expected) {
        boolean ok = actual != null && actual.equals(expected);
        System.out.println((ok ? "PASS " : "FAIL ") + name + ": got=" + actual + (ok ? "" : "  expected=" + expected));
        if (ok) pass++; else fail++;
    }

    static class CapturePlayer implements Player {
        int seat = -1; Message last;
        public String getId() { return "cap"; }
        public String getName() { return "Cap"; }
        public boolean isAi() { return false; }
        public void setSeat(int s) { seat = s; }
        public int getSeat() { return seat; }
        public void send(Message msg) { last = msg; }
    }
}
