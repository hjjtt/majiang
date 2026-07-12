import type { Envelope } from '../protocol/messages';

/** tt.connectSocket 返回的 Socket 任务对象。 */
interface SocketTask {
  send(opts: { data: string }): void;
  close(opts?: { code?: number; reason?: string }): void;
  onOpen(cb: () => void): void;
  onMessage(cb: (res: { data: string | ArrayBuffer }) => void): void;
  onClose(cb: () => void): void;
  onError(cb: (err: unknown) => void): void;
}

type Handler = (msg: Envelope) => void;

/**
 * WebSocket 客户端封装（基于 tt.connectSocket）。
 * - {@link request}：请求-响应（按 seq 自动配对，返回 Promise，带超时）
 * - {@link send}：单向发送（完整信封）
 * - {@link on}：订阅某类广播消息，返回取消订阅函数
 */
export class SocketClient {
  private socket: SocketTask | null = null;
  private seq = 0;
  private readonly pending = new Map<number, (msg: Envelope) => void>();
  private readonly handlers = new Map<string, Handler[]>();

  /** 连接 WebSocket 服务，握手成功后 resolve。 */
  connect(url: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      const socket = tt.connectSocket({ url }) as SocketTask;
      this.socket = socket;
      socket.onOpen(() => resolve());
      socket.onError((err) => reject(err));
      socket.onClose(() => this.cleanup());
      socket.onMessage((res) => {
        if (typeof res.data === 'string') this.onRaw(res.data);
      });
    });
  }

  private onRaw(raw: string): void {
    let msg: Envelope;
    try {
      msg = JSON.parse(raw) as Envelope;
    } catch {
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
    if (arr) arr.slice().forEach((h) => h(msg));
  }

  /** 请求-响应：发送并等待同 seq 的响应。 */
  request(type: string, data?: unknown, timeoutMs = 10000): Promise<Envelope> {
    if (!this.socket) return Promise.reject(new Error('未连接'));
    const seq = ++this.seq;
    return new Promise<Envelope>((resolve, reject) => {
      const timer = setTimeout(() => {
        if (this.pending.delete(seq)) reject(new Error(`请求超时: ${type}`));
      }, timeoutMs);
      this.pending.set(seq, (msg) => {
        clearTimeout(timer);
        resolve(msg);
      });
      this.send({ type, seq, data });
    });
  }

  /** 单向发送（完整信封）。 */
  send(msg: Envelope): void {
    if (!this.socket) throw new Error('未连接');
    this.socket.send({ data: JSON.stringify(msg) });
  }

  /** 订阅某类广播消息，返回取消订阅函数。 */
  on(type: string, handler: Handler): () => void {
    let arr = this.handlers.get(type);
    if (!arr) {
      arr = [];
      this.handlers.set(type, arr);
    }
    arr.push(handler);
    return () => {
      const list = this.handlers.get(type);
      if (!list) return;
      const i = list.indexOf(handler);
      if (i >= 0) list.splice(i, 1);
    };
  }

  close(): void {
    this.socket?.close();
    this.cleanup();
  }

  private cleanup(): void {
    this.socket = null;
    this.pending.clear();
    this.handlers.clear();
  }
}
