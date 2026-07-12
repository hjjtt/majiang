/**
 * 牌桌场景：Canvas 绘制 + 点击出牌。
 *
 * 布局（竖屏）：
 *   上方    对家(seat2) 手牌（背面/正面朝下）
 *   左右    西家(3) / 东家(1) 竖排手牌
 *   中间    出牌河（弃牌区）
 *   下方    自己(seat0) 手牌，可点击出牌
 */
import type { GameClient } from '../game/GameClient';

/** 单张手牌的命中盒。 */
interface HitBox {
  tile: string;
  x: number;
  y: number;
  w: number;
  h: number;
}

/** 各座位的手牌（seat0 为自己，可见；其余背面）。 */
type Hands = { [seat: number]: string[] };

const TILE_W = 38;
const TILE_H = 54;
const GREEN = '#0a7a4f';
const GREEN_DARK = '#075c3b';
const TILE_BG = '#f4f1e6';
const TILE_BORDER = '#c9c0a8';
const TEXT_DARK = '#1a1d29';
const TEXT_RED = '#c0392b';

/**
 * 牌桌场景。
 * 生命周期：new BoardScene(ctx,w,h,canvas) → draw() 画空桌 →
 *           attach(client, seat) 绑定客户端与座位 →
 *           订阅事件后内部重绘 → tap(x,y) 触发出牌。
 */
export class BoardScene {
  private client: GameClient | null = null;
  private mySeat = 0;

  private hands: Hands = { 0: [], 1: [], 2: [], 3: [] };
  /** 当前轮到谁（seat），-1 表示未知。 */
  private turnSeat = -1;
  /** 自己刚摸的牌（摸牌后高亮，点击可出）。 */
  private drawnTile: string | null = null;

  private hitBoxes: HitBox[] = [];

  constructor(
    private ctx: CanvasRenderingContext2D,
    private w: number,
    private h: number,
  ) {}

  /** 绑定客户端与本玩家座位，并订阅牌局事件驱动重绘。 */
  attach(client: GameClient, mySeat: number): void {
    this.client = client;
    this.mySeat = mySeat;

    client.onDealTiles((d) => {
      this.hands[this.mySeat] = (d.hand || []).slice();
      this.drawnTile = null;
      this.draw();
    });
    client.onDrawTile((d) => {
      this.hands[this.mySeat] = [...(this.hands[this.mySeat] || []), d.tile];
      this.drawnTile = d.tile;
      this.draw();
    });
    client.onTileDiscarded((d) => {
      // 任意座出牌：从其手牌移除一张同名牌
      const arr = this.hands[d.seat] || [];
      const idx = arr.indexOf(d.tile);
      if (idx >= 0) arr.splice(idx, 1);
      if (d.seat === this.mySeat) this.drawnTile = null;
      this.draw();
    });
    client.onTurnChange((d) => {
      this.turnSeat = d.seat;
      this.draw();
    });
  }

  /** 画整张牌桌。同时重建自己手牌的命中盒。 */
  draw(): void {
    const { ctx, w, h } = this;
    // 桌布
    ctx.fillStyle = GREEN;
    ctx.fillRect(0, 0, w, h);
    ctx.fillStyle = GREEN_DARK;
    ctx.beginPath();
    ctx.ellipse(w / 2, h / 2, w * 0.42, h * 0.28, 0, 0, Math.PI * 2);
    ctx.fill();

    this.drawSeatLabel(2, w / 2, 70);   // 对家
    this.drawSeatLabel(1, w - 60, h / 2); // 东家（右）
    this.drawSeatLabel(3, 60, h / 2);     // 西家（左）
    this.drawSeatLabel(0, w / 2, h - 120); // 自己

    // 对家/左右家：画牌背占位（张数）
    this.drawBacks(2, w / 2, 100, 'h');
    this.drawBacks(1, w - 90, h / 2 - 60, 'v');
    this.drawBacks(3, 50, h / 2 - 60, 'v');

    // 自己手牌（底部，可点击）
    this.hitBoxes = [];
    this.drawMyHand();

    // 空状态：尚未发牌时，桌中央给出引导提示（符合 UX empty-state 规范）
    if ((this.hands[0] || []).length === 0) {
      ctx.fillStyle = 'rgba(255,255,255,.85)';
      ctx.font = '18px Arial';
      ctx.textAlign = 'center';
      ctx.fillText('等待发牌…', w / 2, h / 2 - 8);
      ctx.fillStyle = 'rgba(255,255,255,.5)';
      ctx.font = '13px Arial';
      ctx.fillText('正在连接对局服务器', w / 2, h / 2 + 18);
      ctx.textAlign = 'left';
    }
  }

  /** 点击处理：命中自己手牌则出牌。 */
  tap(x: number, y: number): void {
    for (const hb of this.hitBoxes) {
      if (x >= hb.x && x <= hb.x + hb.w && y >= hb.y && y <= hb.y + hb.h) {
        this.client?.discard(hb.tile);
        return;
      }
    }
  }

  // ---- 私有绘制工具 ----

  private drawSeatLabel(seat: number, cx: number, cy: number): void {
    const { ctx } = this;
    const names = ['自己', '东家', '对家', '西家'];
    const isTurn = this.turnSeat === seat;
    ctx.fillStyle = isTurn ? '#ffd54a' : 'rgba(255,255,255,.6)';
    ctx.font = '13px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(names[seat] + (isTurn ? ' ●' : ''), cx, cy);
    ctx.textAlign = 'left';
  }

  /** 画背面手牌占位（其他玩家）。dir: h 横排 / v 竖排。 */
  private drawBacks(seat: number, x: number, y: number, dir: 'h' | 'v'): void {
    const { ctx } = this;
    const count = (this.hands[seat] || []).length;
    const step = dir === 'h' ? 16 : 16;
    for (let i = 0; i < count; i++) {
      const ox = dir === 'h' ? x + i * step : x;
      const oy = dir === 'v' ? y + i * step : y;
      ctx.fillStyle = '#2b6e54';
      ctx.strokeStyle = '#1b4a37';
      ctx.lineWidth = 1;
      ctx.fillRect(ox, oy, dir === 'h' ? 14 : TILE_W * 0.5, dir === 'h' ? TILE_H * 0.5 : 14);
      ctx.strokeRect(ox, oy, dir === 'h' ? 14 : TILE_W * 0.5, dir === 'h' ? TILE_H * 0.5 : 14);
    }
  }

  /** 画自己手牌（底部横排），并记录命中盒。 */
  private drawMyHand(): void {
    const tiles = this.hands[0] || [];
    if (tiles.length === 0) return;
    const { w, h } = this;
    const gap = 4;
    const totalW = tiles.length * TILE_W + (tiles.length - 1) * gap;
    const startX = (w - totalW) / 2;
    const baseY = h - 90;

    for (let i = 0; i < tiles.length; i++) {
      const t = tiles[i];
      // 最后一张（刚摸的）若为 drawnTile，右侧留缝并稍微上抬
      const isDrawn = this.drawnTile !== null && i === tiles.length - 1 && t === this.drawnTile;
      const x = startX + i * (TILE_W + gap) + (isDrawn ? gap * 3 : 0);
      const y = isDrawn ? baseY - 10 : baseY;
      this.drawTile(x, y, TILE_W, TILE_H, t);
      this.hitBoxes.push({ tile: t, x, y, w: TILE_W, h: TILE_H });
    }
  }

  /** 画单张麻将牌（正面），含编码转文字。 */
  private drawTile(x: number, y: number, tw: number, th: number, code: string): void {
    const { ctx } = this;
    // 牌身
    ctx.fillStyle = TILE_BG;
    ctx.strokeStyle = TILE_BORDER;
    ctx.lineWidth = 1;
    this.roundRect(x, y, tw, th, 4);
    ctx.fill();
    ctx.stroke();
    // 文字
    const { text, color } = this.tileFace(code);
    ctx.fillStyle = color;
    ctx.font = 'bold 20px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(text, x + tw / 2, y + th / 2 + 1);
    ctx.textAlign = 'left';
    ctx.textBaseline = 'alphabetic';
  }

  /** 牌编码 → 显示文字 + 颜色。 */
  private tileFace(code: string): { text: string; color: string } {
    if (!code) return { text: '?', color: TEXT_DARK };
    const c = code.charAt(0);
    const n = code.slice(1);
    // 万 W / 条 T / 筒 D
    if (c === 'W' || c === 'T' || c === 'D') {
      const numMap: { [k: string]: string } = { '1': '一', '2': '二', '3': '三', '4': '四', '5': '五', '6': '六', '7': '七', '8': '八', '9': '九' };
      const unit = c === 'W' ? '万' : c === 'T' ? '条' : '筒';
      const color = c === 'T' ? TEXT_DARK : c === 'D' ? '#2980b9' : TEXT_DARK;
      return { text: (numMap[n] || n) + unit, color };
    }
    // 字牌 J1-7 → 东南西北白发中
    const honorMap: { [k: string]: string } = { '1': '东', '2': '南', '3': '西', '4': '北', '5': '白', '6': '发', '7': '中' };
    if (c === 'J') {
      const text = honorMap[n] || '?';
      const color = (n === '5' || n === '7') ? TEXT_RED : (n === '6' ? TEXT_RED : TEXT_DARK);
      return { text, color };
    }
    return { text: code, color: TEXT_DARK };
  }

  /** 圆角矩形路径（不自动 fill/stroke，由调用方执行）。 */
  private roundRect(x: number, y: number, w: number, h: number, r: number): void {
    const { ctx } = this;
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.arcTo(x + w, y, x + w, y + h, r);
    ctx.arcTo(x + w, y + h, x, y + h, r);
    ctx.arcTo(x, y + h, x, y, r);
    ctx.arcTo(x, y, x + w, y, r);
    ctx.closePath();
  }
}
