// pages/result/result.js
Page({
  data: {
    winner: -1,
    selfDraw: false,
    han: 0,
    fu: 0,
    points: 0,
    yaku: [],
    isWin: false,
  },

  onLoad(opts) {
    const winner = Number(opts.winner);
    const selfDraw = opts.selfDraw === '1';
    const han = Number(opts.han) || 0;
    const fu = Number(opts.fu) || 0;
    const points = Number(opts.points) || 0;
    // isWin 由 game 页按 mySeat 比对后传入（1=自己赢）
    const isWin = opts.isWin === '1';
    // banner 文案：自己和牌 / 别人和牌 / 流局无人和
    let banner;
    if (winner < 0) {
      banner = '流局';
    } else if (isWin) {
      banner = '和牌';
    } else {
      banner = '未和牌';
    }
    this.setData({ winner, selfDraw, han, fu, points, isWin, banner });
  },

  goHome() {
    tt.switchTab({ url: '/pages/home/home' });
  },

  playAgain() {
    tt.redirectTo({ url: '/pages/game/game?mode=pve' });
  },
});
