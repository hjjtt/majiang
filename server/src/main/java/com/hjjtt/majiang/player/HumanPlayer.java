package com.hjjtt.majiang.player;

import com.hjjtt.majiang.game.Player;
import com.hjjtt.majiang.message.Message;
import com.hjjtt.majiang.netty.GameFrameHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 真人玩家：通过 WebSocket 通信。send 直接转发到 GameFrameHandler 下发。
 */
public class HumanPlayer implements Player {

    private final String id;
    private final String name;
    private final ChannelHandlerContext ctx;
    private int seat = -1;

    public HumanPlayer(String id, String name, ChannelHandlerContext ctx) {
        this.id = id;
        this.name = name;
        this.ctx = ctx;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public boolean isAi() { return false; }
    @Override public void setSeat(int seat) { this.seat = seat; }
    @Override public int getSeat() { return seat; }

    @Override
    public void send(Message msg) {
        GameFrameHandler.send(ctx, msg);
    }

    public ChannelHandlerContext context() { return ctx; }

    private String roomId;
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomId() { return roomId; }
}
