package com.hjjtt.majiang.room;

import com.hjjtt.majiang.game.Game;
import com.hjjtt.majiang.game.MahjongRule;
import com.hjjtt.majiang.game.Player;
import com.hjjtt.majiang.player.AiPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 房间管理：创建房间、匹配入座、人机补位。
 * 骨架阶段使用内存存储 + 简单匹配；生产环境需考虑分布式与持久化。
 */
public class RoomManager {

    private final ConcurrentMap<String, Game> rooms = new ConcurrentHashMap<>();
    private final MahjongRule rule;

    /** pvp 模式下等待凑齐 4 人的房间。 */
    private volatile Game pendingPvp;

    public RoomManager(MahjongRule rule) {
        this.rule = rule;
    }

    /**
     * 请求匹配，入座并返回房间与座位。
     *
     * @param mode "pvp" 真人对战 / "pve" 人机（自动补 3 个 AI） / "mix" 混合
     *             （mix 当前按 pvp 处理，凑满 4 真人；补 AI 逻辑后续细化）
     */
    public synchronized MatchResult match(Player p, String mode) {
        Game game;
        if ("pve".equalsIgnoreCase(mode)) {
            game = register(new Game(rule));
            game.sitDown(p);
            fillAi(game, 3);
            game.start();
        } else {
            // pvp / mix：共享一个等待房，凑满 4 人开局
            if (pendingPvp == null || pendingPvp.isFull()) {
                pendingPvp = register(new Game(rule));
            }
            game = pendingPvp;
            game.sitDown(p);
            if (game.isFull()) {
                game.start();
                pendingPvp = null;
            }
        }
        return new MatchResult(game.getRoomId(), p.getSeat());
    }

    public Game find(String roomId) {
        return rooms.get(roomId);
    }

    private Game register(Game g) {
        rooms.put(g.getRoomId(), g);
        return g;
    }

    private void fillAi(Game game, int n) {
        for (int i = 0; i < n; i++) {
            game.sitDown(new AiPlayer(
                    "bot_" + UUID.randomUUID().toString().substring(0, 4),
                    "机器人" + (i + 1)));
        }
    }

    /** 匹配结果。 */
    public record MatchResult(String roomId, int seat) {}
}
