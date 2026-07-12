/**
 * 牌桌场景：Canvas 绘制骨架。
 * 骨架阶段只画桌面背景与提示；手牌、出牌区、动画随规则与 UI 细化。
 */
export class BoardScene {
  constructor(
    private ctx: CanvasRenderingContext2D,
    private w: number,
    private h: number,
  ) {}

  draw(): void {
    const { ctx, w, h } = this;
    ctx.fillStyle = '#0a7a4f'; // 桌布绿
    ctx.fillRect(0, 0, w, h);
    ctx.fillStyle = '#e8f5e9';
    ctx.font = '14px Arial';
    ctx.fillText('牌桌场景骨架（座位 / 手牌待绘制）', 20, h - 20);
    // TODO: 按 GameClient.currentSeat 绘制四个座位与手牌
  }
}
