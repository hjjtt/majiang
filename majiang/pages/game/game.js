// pages/game/game.js
const { GameClient } = require('../../js/game/GameClient');
const { BoardScene } = require('../../js/scene/board');

Page({
  data: {
    roundName: '东1局',
    timer: 15,
    mySeat: 0,
    // 动作栏：别人出牌后弹出，供自己选择是否声明
    showActions: false,
    canHu: false,
    canGang: false,
    canPong: false,
    canRiichi: false,
    // 结算遮罩
    showResult: false,
    result: null,
  },

  /** @type {BoardScene|null} */
  board: null,
  /** @type {GameClient|null} */
  client: null,
  /** 页面参数（mode/roomId）。 */
  options: { mode: 'pve', roomId: '' },
  /** 上次点炮的牌，供声明时携带（协议 claim 暂不强制 tiles）。 */
  lastDiscard: null,

  onLoad(options) {
    this.options = {
      mode: options.mode || 'pve',
      roomId: options.roomId || '',
    };
  },

  onReady() {
    const app = getApp();
    const socketUrl = (app && app.globalData && app.globalData.socketUrl) || 'ws://localhost:9000/ws';

    // Canvas 2D 节点在 onReady 后未必立即可用，需延时重试（抖音已知行为）
    this.initCanvas(socketUrl);
  },

  initCanvas(socketUrl, retry) {
    const tryCount = (retry || 0) + 1;
    const query = tt.createSelectorQuery();
    query.select('#gameCanvas')
      .fields({ node: true, size: true })
      .exec((res) => {
        const info = res && res[0];
        if (!info || !info.node || !info.width) {
          // 节点未就绪：延时重试，最多 5 次（间隔递增 100/200/300/400ms）
          if (tryCount <= 5) {
            console.warn('[game] canvas 节点未就绪，第', tryCount, '次重试');
            setTimeout(() => this.initCanvas(socketUrl, tryCount), tryCount * 100);
          } else {
            console.error('[game] canvas 节点获取失败，放弃');
            tt.showToast({ title: '牌桌加载失败', icon: 'none' });
          }
          return;
        }

        const canvas = info.node;
        const ctx = canvas.getContext('2d');
        const dpr = tt.getSystemInfoSync().pixelRatio;

        canvas.width = info.width * dpr;
        canvas.height = info.height * dpr;
        ctx.scale(dpr, dpr);

        // 先画空桌，保证页面立即有内容，不阻塞在后端连接上
        this.board = new BoardScene(ctx, info.width, info.height);
        this.board.draw();
        this.startMatch(socketUrl);
      });
  },

  async startMatch(socketUrl) {
    this.client = new GameClient();
    try {
      await this.client.connect(socketUrl);
      await this.client.login('dev-token');
      const matchData = await this.client.match(this.options.mode);
      if (this.board && matchData) {
        this.setData({ mySeat: matchData.seat });
        this.board.attach(this.client, matchData.seat);
      }
      this.bindEvents();
    } catch (err) {
      console.error('startMatch failed:', err);
      tt.showToast({ title: '连接失败', icon: 'none' });
    }
  },

  bindEvents() {
    if (!this.client) return;

    // 轮次变化：轮到自己摸/出牌时隐藏动作栏
    this.client.onTurnChange((d) => {
      this.setData({
        showActions: false,
        canHu: false, canGang: false, canPong: false, canRiichi: false,
      });
    });

    // 别人出牌后：若点炮牌不是自己出的，弹出动作栏（可选择是否声明）
    this.client.onTileDiscarded((d) => {
      this.lastDiscard = d;
      if (d.seat !== this.data.mySeat) {
        // 简化：默认全部可声明，由服务端校验拒绝。客户端无法精确判定可胡/可碰。
        this.setData({
          showActions: true,
          canHu: true, canGang: true, canPong: true, canRiichi: false,
        });
      }
    });

    this.client.onGameOver((d) => {
      this.setData({ showResult: true, result: d });
    });

    this.client.onError((d) => {
      // 声明被拒（如 FURITEN/RIICHI_LOCK）：提示后关闭动作栏
      tt.showToast({ title: d.message || d.code || '操作无效', icon: 'none' });
      this.setData({ showActions: false });
    });
  },

  onTouchStart(e) {
    if (!this.board) return;
    const touch = e.touches[0];
    this.board.tap(touch.x, touch.y);
  },

  onHu() {
    if (this.client) this.client.claim('hu', []);
    this.setData({ showActions: false });
  },

  onGang() {
    if (this.client) this.client.claim('gang', []);
    this.setData({ showActions: false });
  },

  onPong() {
    if (this.client) this.client.claim('peng', []);
    this.setData({ showActions: false });
  },

  onRiichi() {
    if (this.client) this.client.claim('riichi', []);
    this.setData({ showActions: false });
  },

  onPass() {
    // 协议无 pass：不发消息，靠轮次自动流转。仅关闭动作栏。
    this.setData({ showActions: false });
  },

  goResult() {
    const r = this.data.result || {};
    // 胜负由 result.winner 与 mySeat 比较
    const isWin = r.winner === this.data.mySeat ? 1 : 0;
    const params = 'winner=' + (r.winner !== undefined ? r.winner : -1)
      + '&selfDraw=' + (r.selfDraw ? 1 : 0)
      + '&han=' + (r.han || 0)
      + '&fu=' + (r.fu || 0)
      + '&points=' + (r.points || 0)
      + '&isWin=' + isWin;
    tt.navigateTo({ url: '/pages/result/result?' + params });
  },

  onUnload() {
    if (this.client) this.client.close();
  },
});
