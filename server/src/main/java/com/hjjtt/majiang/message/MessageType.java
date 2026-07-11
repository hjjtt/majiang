package com.hjjtt.majiang.message;

/**
 * 消息类型常量。取值与 protocol/messages.md 的 type 字段一一对应。
 */
public final class MessageType {
    private MessageType() {}

    public static final String LOGIN         = "login";
    public static final String LOGIN_RESULT  = "loginResult";
    public static final String MATCH         = "match";
    public static final String MATCH_RESULT  = "matchResult";
    public static final String DEAL_TILES    = "dealTiles";
    public static final String TURN_CHANGE   = "turnChange";
    public static final String DRAW_TILE     = "drawTile";
    public static final String DISCARD       = "discard";
    public static final String TILE_DISCARDED = "tileDiscarded";
    public static final String CLAIM         = "claim";
    public static final String CLAIM_RESULT  = "claimResult";
    public static final String GAME_OVER     = "gameOver";
    public static final String ERROR         = "error";
}
