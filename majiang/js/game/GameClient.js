"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.GameClient = void 0;
const SocketClient_1 = require("../net/SocketClient");
const messages_1 = require("../protocol/messages");
/**
 * 游戏客户端：封装登录/匹配/出牌等高层操作，并暴露牌局事件订阅。
 * 上层（Cocos 场景脚本）只与本类交互，不直接接触 SocketClient。
 */
class GameClient {
    constructor() {
        this.net = new SocketClient_1.SocketClient();
    }
    /** 连接 WebSocket 服务。 */
    async connect(url) {
        await this.net.connect(url);
    }
    /** 登录。成功后记录玩家身份。 */
    async login(token) {
        const resp = await this.net.request(messages_1.MessageType.LOGIN, { token });
        const data = resp.data;
        if (data === null || data === void 0 ? void 0 : data.ok)
            this.playerId = data.playerId;
        return data;
    }
    /** 请求匹配。成功后记录房间与座位。 */
    async match(mode) {
        const resp = await this.net.request(messages_1.MessageType.MATCH, { mode });
        const data = resp.data;
        if (data) {
            this.roomId = data.roomId;
            this.seat = data.seat;
        }
        return data;
    }
    /** 出牌（客户端→服务端，规则由服务端校验）。 */
    discard(tile) {
        this.net.send({ type: messages_1.MessageType.DISCARD, seq: 0, data: { tile } });
    }
    /** 声明动作（吃/碰/杠/胡）。 */
    claim(action, tiles) {
        this.net.send({ type: messages_1.MessageType.CLAIM, seq: 0, data: { action, tiles } });
    }
    // ---- 牌局事件订阅（S→C 广播）----
    onDealTiles(h) { return this.subscribe(messages_1.MessageType.DEAL_TILES, h); }
    onTurnChange(h) { return this.subscribe(messages_1.MessageType.TURN_CHANGE, h); }
    onDrawTile(h) { return this.subscribe(messages_1.MessageType.DRAW_TILE, h); }
    onTileDiscarded(h) { return this.subscribe(messages_1.MessageType.TILE_DISCARDED, h); }
    onClaimResult(h) { return this.subscribe(messages_1.MessageType.CLAIM_RESULT, h); }
    onGameOver(h) { return this.subscribe(messages_1.MessageType.GAME_OVER, h); }
    onError(h) { return this.subscribe(messages_1.MessageType.ERROR, h); }
    subscribe(type, h) {
        return this.net.on(type, (msg) => h(msg.data));
    }
    close() { this.net.close(); }
    get currentSeat() { return this.seat; }
    get currentRoomId() { return this.roomId; }
    get currentPlayerId() { return this.playerId; }
}
exports.GameClient = GameClient;
//# sourceMappingURL=GameClient.js.map