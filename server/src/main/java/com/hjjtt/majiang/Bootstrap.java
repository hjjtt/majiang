package com.hjjtt.majiang;

import com.hjjtt.majiang.game.MahjongRule;
import com.hjjtt.majiang.game.RiichiRule;
import com.hjjtt.majiang.netty.WebSocketServer;
import com.hjjtt.majiang.room.RoomManager;

/**
 * 服务端启动入口。
 * 运行：mvn compile exec:java   或   mvn compile exec:java -Dexec.args="9000"
 */
public class Bootstrap {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9000;
        MahjongRule rule = new RiichiRule();
        RoomManager rooms = new RoomManager(rule);
        new WebSocketServer(port, rooms).start();
    }
}
