package com.hjjtt.majiang.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjjtt.majiang.message.Message;
import com.hjjtt.majiang.message.MessageType;
import com.hjjtt.majiang.player.HumanPlayer;
import com.hjjtt.majiang.room.RoomManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 处理 WebSocket 文本帧：解析 JSON 信封并按 type 分发。
 */
public class GameFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(GameFrameHandler.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final AttributeKey<HumanPlayer> PLAYER = AttributeKey.valueOf("player");

    private final RoomManager rooms;

    public GameFrameHandler(RoomManager rooms) {
        this.rooms = rooms;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (!(frame instanceof TextWebSocketFrame text)) {
            return; // 仅处理文本帧（JSON）
        }
        try {
            Message msg = JSON.readValue(text.text(), Message.class);
            log.info("recv: {}", msg.getType());
            dispatch(ctx, msg);
        } catch (Exception e) {
            log.warn("消息解析失败: {}", e.getMessage());
        }
    }

    private void dispatch(ChannelHandlerContext ctx, Message msg) {
        switch (msg.getType()) {
            case MessageType.LOGIN -> handleLogin(ctx, msg);
            case MessageType.MATCH -> handleMatch(ctx, msg);
            default -> log.warn("未知消息类型: {}", msg.getType());
        }
    }

    // TODO: 校验 token。骨架阶段直接签发玩家身份并绑定到 channel。
    private void handleLogin(ChannelHandlerContext ctx, Message msg) {
        String playerId = "p_" + ctx.channel().hashCode();
        HumanPlayer p = new HumanPlayer(playerId, "玩家" + playerId, ctx);
        ctx.channel().attr(PLAYER).set(p);
        send(ctx, Message.reply(msg.getSeq(), MessageType.LOGIN_RESULT,
                Map.of("ok", true, "playerId", playerId, "name", p.getName())));
    }

    private void handleMatch(ChannelHandlerContext ctx, Message msg) {
        HumanPlayer p = ctx.channel().attr(PLAYER).get();
        if (p == null) {
            send(ctx, Message.reply(msg.getSeq(), MessageType.ERROR,
                    Map.of("code", "NOT_LOGGED_IN", "message", "请先登录")));
            return;
        }
        String mode = "pvp";
        if (msg.getData() != null && msg.getData().get("mode") != null) {
            mode = String.valueOf(msg.getData().get("mode"));
        }
        RoomManager.MatchResult r = rooms.match(p, mode);
        send(ctx, Message.reply(msg.getSeq(), MessageType.MATCH_RESULT,
                Map.of("roomId", r.roomId(), "seat", r.seat(), "mode", mode)));
    }

    public static void send(ChannelHandlerContext ctx, Message msg) {
        try {
            ctx.writeAndFlush(new TextWebSocketFrame(JSON.writeValueAsString(msg)));
        } catch (Exception e) {
            log.error("发送失败: {}", e.getMessage());
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("连接加入: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("连接关闭: {}", ctx.channel().remoteAddress());
    }
}
