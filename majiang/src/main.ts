import { GameClient } from './game/GameClient';
import { BoardScene } from './scene/board';

// 小游戏运行时初始化
const sys = tt.getSystemInfoSync();
const canvas = tt.createCanvas();
const ctx: CanvasRenderingContext2D = canvas.getContext('2d');
canvas.width = sys.windowWidth;
canvas.height = sys.windowHeight;

const board = new BoardScene(ctx, canvas.width, canvas.height);
board.draw();

/** 顶部状态条（覆盖重绘）。 */
function setStatus(text: string, color = '#fff'): void {
  ctx.fillStyle = '#0a7a4f';
  ctx.fillRect(0, 0, sys.windowWidth, 80);
  ctx.fillStyle = color;
  ctx.font = '16px Arial';
  ctx.fillText(text, 20, 45);
}
setStatus('连接中...');

// 启动流程：连接 -> 登录 -> 匹配（pve：1 真人 + 3 AI）
const client = new GameClient();
client
  .connect('ws://localhost:9000/ws')
  .then(() => {
    setStatus('已连接，登录中...');
    return client.login('dev-token');
  })
  .then((r) => {
    setStatus('已登录: ' + r.name);
    return client.match('pve');
  })
  .then((m) => {
    setStatus('已入房 ' + m.roomId + ' 座位 ' + m.seat);
  })
  .catch((e) => {
    setStatus('失败: ' + String(e), '#f88');
  });

// TODO: 订阅牌局事件（onDealTiles/onTurnChange...）并驱动 BoardScene 重绘
