package com.hjjtt.majiang;

import com.hjjtt.majiang.game.Game;
import com.hjjtt.majiang.game.RiichiRule;
import com.hjjtt.majiang.player.AiPlayer;

/**
 * 集成测试：4 个 AI 跑完一局，确认状态机闭环 + settleWin 新路径不崩。
 * 运行需 slf4j/logback 在 classpath（Game 用了日志）：
 *   mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
 *   java -cp "target/classes;$(cat cp.txt)" com.hjjtt.majiang.GameRunTest
 */
public class GameRunTest {
    public static void main(String[] args) throws Exception {
        Game g = new Game(new RiichiRule());
        for (int i = 0; i < 4; i++) g.sitDown(new AiPlayer("b" + i, "AI" + i));
        g.start();
        Thread.sleep(15000);
        boolean ok = g.getPhase() == Game.Phase.SETTLED;
        System.out.println("phase=" + g.getPhase() + " " + (ok ? "PASS" : "FAIL") + " (期望 SETTLED)");
        System.exit(ok ? 0 : 1);
    }
}
