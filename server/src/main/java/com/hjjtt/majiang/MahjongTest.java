package com.hjjtt.majiang;

import com.hjjtt.majiang.game.RiichiRule;
import com.hjjtt.majiang.game.Score;

import java.util.List;
import java.util.Objects;

/**
 * 核心可玩日麻算法自测（main 方法，无需测试框架）。
 * 运行：mvn -q compile && java -cp target/classes com.hjjtt.majiang.MahjongTest
 */
public class MahjongTest {
    static int pass = 0, fail = 0;

    public static void main(String[] args) {
        RiichiRule r = new RiichiRule();

        // 1. 断幺九（门前荣和，子家）
        List<String> tanyao = List.of("W2","W2","W2","W3","W4","W5","T5","T6","T7","D4","D5","D6","D7","D7");
        chk("tanyao.yaku", r.findYaku(tanyao, false, false, true), List.of("tanyao"));
        Score s1 = r.score(tanyao, false, false, true, false, true);
        chk("tanyao.han", s1.han(), 1);
        chk("tanyao.points", s1.totalPoints(), 1000);

        // 2. 清一色（非九莲结构：缺 W1 端张）
        List<String> chin = List.of("W2","W2","W2","W3","W4","W5","W5","W6","W7","W7","W8","W9","W9","W9");
        chk("chin.yaku", r.findYaku(chin, false, false, true), List.of("chinitsu"));
        Score s2 = r.score(chin, false, false, true, false, true);
        chk("chin.han", s2.han(), 6);
        chk("chin.points", s2.totalPoints(), 12000);

        // 3. 七对
        List<String> chii = List.of("W1","W1","W2","W2","W3","W3","T1","T1","T2","T2","D1","D1","D2","D2");
        chk("chiitoi.yaku", r.findYaku(chii, false, false, true), List.of("chiitoitsu"));
        Score s3 = r.score(chii, false, false, true, false, true);
        chk("chiitoi.fu", s3.fu(), 25);
        chk("chiitoi.points", s3.totalPoints(), 1600);

        // 4. 对对和
        List<String> toi = List.of("W2","W2","W2","T3","T3","T3","D4","D4","D4","J1","J1","J1","J5","J5");
        chk("toitoi.yaku", r.findYaku(toi, false, false, true), List.of("toitoi"));
        Score s4 = r.score(toi, false, false, true, false, true);
        chk("toitoi.han", s4.han(), 2);
        chk("toitoi.points", s4.totalPoints(), 2000);

        // 5. 立直+门前自摸+断幺九（子家，含一发）
        chk("riichi.yaku", r.findYaku(tanyao, true, true, true), List.of("riichi","tsumo","ippatsu","tanyao"));
        Score s5 = r.score(tanyao, true, true, true, false, false);
        chk("riichi.han", s5.han(), 4);
        chk("riichi.points", s5.totalPoints(), 7700);

        // 6. 无役 -> 不可胡（score 返回 null）
        List<String> noYaku = List.of("W1","W2","W3","W4","W5","W6","T7","T8","T9","D1","D2","D3","J1","J1");
        chk("noYaku.yaku", r.findYaku(noYaku, false, false, true), List.of());
        chk("noYaku.score", r.score(noYaku, false, false, true, false, true), null);

        System.out.println("\n==== " + pass + " pass, " + fail + " fail ====");
        if (fail > 0) System.exit(1);
    }

    static void chk(String name, Object actual, Object expected) {
        boolean ok = Objects.equals(actual, expected);
        System.out.println((ok ? "PASS " : "FAIL ") + name + ": got=" + actual + (ok ? "" : "  expected=" + expected));
        if (ok) pass++; else fail++;
    }
}
