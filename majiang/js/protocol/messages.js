"use strict";
/**
 * 通信协议类型定义。与后端 protocol/messages.md 一一对应。
 * 消息信封：{ type: string, seq: number, data? }
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.MessageType = void 0;
/** 消息类型常量 */
exports.MessageType = {
    LOGIN: 'login',
    LOGIN_RESULT: 'loginResult',
    MATCH: 'match',
    MATCH_RESULT: 'matchResult',
    DEAL_TILES: 'dealTiles',
    TURN_CHANGE: 'turnChange',
    DRAW_TILE: 'drawTile',
    DISCARD: 'discard',
    TILE_DISCARDED: 'tileDiscarded',
    CLAIM: 'claim',
    CLAIM_RESULT: 'claimResult',
    GAME_OVER: 'gameOver',
    ERROR: 'error',
};
//# sourceMappingURL=messages.js.map