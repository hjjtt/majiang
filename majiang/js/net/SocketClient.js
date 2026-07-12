"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SocketClient = void 0;
/**
 * WebSocket 客户端封装（基于 tt.connectSocket）。
 * - {@link request}：请求-响应（按 seq 自动配对，返回 Promise，带超时）
 * - {@link send}：单向发送（完整信封）
 * - {@link on}：订阅某类广播消息，返回取消订阅函数
 */
class SocketClient {
    constructor() {
        this.socket = null;
        this.seq = 0;
        this.pending = new Map();
        this.handlers = new Map();
    }
    /** 连接 WebSocket 服务，握手成功后 resolve。 */
    connect(url) {
        return new Promise((resolve, reject) => {
            const socket = tt.connectSocket({ url });
            this.socket = socket;
            socket.onOpen(() => resolve());
            socket.onError((err) => reject(err));
            socket.onClose(() => this.cleanup());
            socket.onMessage((res) => {
                if (typeof res.data === 'string')
                    this.onRaw(res.data);
            });
        });
    }
    onRaw(raw) {
        let msg;
        try {
            msg = JSON.parse(raw);
        }
        catch (_a) {
            return; // 非 JSON，忽略
        }
        // 请求-响应配对
        const settle = this.pending.get(msg.seq);
        if (settle) {
            this.pending.delete(msg.seq);
            settle(msg);
            return;
        }
        // 广播：分发给订阅者
        const arr = this.handlers.get(msg.type);
        if (arr)
            arr.slice().forEach((h) => h(msg));
    }
    /** 请求-响应：发送并等待同 seq 的响应。 */
    request(type, data, timeoutMs = 10000) {
        if (!this.socket)
            return Promise.reject(new Error('未连接'));
        const seq = ++this.seq;
        return new Promise((resolve, reject) => {
            const timer = setTimeout(() => {
                if (this.pending.delete(seq))
                    reject(new Error(`请求超时: ${type}`));
            }, timeoutMs);
            this.pending.set(seq, (msg) => {
                clearTimeout(timer);
                resolve(msg);
            });
            this.send({ type, seq, data });
        });
    }
    /** 单向发送（完整信封）。 */
    send(msg) {
        if (!this.socket)
            throw new Error('未连接');
        this.socket.send({ data: JSON.stringify(msg) });
    }
    /** 订阅某类广播消息，返回取消订阅函数。 */
    on(type, handler) {
        let arr = this.handlers.get(type);
        if (!arr) {
            arr = [];
            this.handlers.set(type, arr);
        }
        arr.push(handler);
        return () => {
            const list = this.handlers.get(type);
            if (!list)
                return;
            const i = list.indexOf(handler);
            if (i >= 0)
                list.splice(i, 1);
        };
    }
    close() {
        var _a;
        (_a = this.socket) === null || _a === void 0 ? void 0 : _a.close();
        this.cleanup();
    }
    cleanup() {
        this.socket = null;
        this.pending.clear();
        this.handlers.clear();
    }
}
exports.SocketClient = SocketClient;
//# sourceMappingURL=SocketClient.js.map