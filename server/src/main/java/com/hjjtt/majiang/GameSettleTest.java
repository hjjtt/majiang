package com.hjjtt.majiang;

import com.hjjtt.majiang.game.Game;
import com.hjjtt.majiang.game.Player;
import com.hjjtt.majiang.game.RiichiRule;
import com.hjjtt.majiang.message.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 必胡集成测试：反射设置可控手牌，直接触发 settleWin，
 * 验证 settleWin -> rule.score -> gameOver 全链路（堵 completion patrol SUSPECT #1）。
 */
public class GameSettleTest {

    public static void main(String[] args) throws Exception {
        RiichiRule rule = new RiichiRule();
        Game g = new Game(rule);
        CapturePlayer[] ps = new CapturePlayer[4];
        for (int i = 0; i < 4; i++) { ps[i] = new CapturePlayer(); g.sitDown(ps[i]); }

        // 座位 0 持 13 张断幺九，最后弃牌 D7 -> 荣和凑成 14 张断幺九（tanyao 1 番）
        setField(g, "dealer", 1);                       // 座位 0 是子家
        setField(g, "lastDiscardTile", "D7");
        @SuppressWarnings("unchecked")
        List<String>[] hands = (List<String>[]) getField(g, "hands");
        hands[0] = new ArrayList<>(List.of(
                "W2","W2","W2","W3","W4","W5","T5","T6","T7","D4","D5","D6","D7"));
        for (int i = 1; i < 4; i++) hands[i] = new ArrayList<>();

        Method m = Game.class.getDeclaredMethod("settleWin", int.class, boolean.class);
        m.setAccessible(true);
        m.invoke(g, 0, false);   // 座位 0 荣和

        int pass = 0, fail = 0;
        for (int i = 0; i < 4; i++) {
            Message last = ps[i].last;
            if (last == null || !"gameOver".equals(last.getType())) {
                System.out.println("FAIL seat" + i + " 未收到 gameOver");
                fail++; continue;
            }
            Map<String, Object> d = last.getData();
            System.out.println("seat" + i + " winner=" + d.get("winner")
                    + " han=" + d.get("han") + " points=" + d.get("points") + " yaku=" + d.get("yaku"));
        }
        Message w0 = ps[0].last;
        if (Integer.valueOf(0).equals(w0.getData().get("winner"))
                && Integer.valueOf(1).equals(w0.getData().get("han"))
                && Integer.valueOf(1000).equals(w0.getData().get("points"))
                && List.of("tanyao").equals(w0.getData().get("yaku"))) {
            System.out.println("PASS settleWin 全链路"); pass++;
        } else {
            System.out.println("FAIL settleWin 字段不符"); fail++;
        }
        System.out.println("==== " + pass + " pass, " + fail + " fail ====");
        System.exit(fail > 0 ? 1 : 0);
    }

    static void setField(Object obj, String name, Object val) throws Exception {
        Field f = Game.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, val);
    }
    static Object getField(Object obj, String name) throws Exception {
        Field f = Game.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    static class CapturePlayer implements Player {
        int seat = -1;
        Message last;
        public String getId() { return "cap"; }
        public String getName() { return "Cap"; }
        public boolean isAi() { return false; }
        public void setSeat(int s) { seat = s; }
        public int getSeat() { return seat; }
        public void send(Message msg) { last = msg; }
    }
}
