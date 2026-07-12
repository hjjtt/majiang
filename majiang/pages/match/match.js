// pages/match/match.js
Page({
  data: {
    selectedMode: 4,
    rooms: [
      { id: 1, name: '樱花小屋', meta: '四人 · 王座之间', icon: '🌸', iconBg: 'rgba(196,69,105,.1)', players: 3, fee: '500' },
      { id: 2, name: '龙王殿', meta: '四人 · 竞技规则', icon: '🐉', iconBg: 'rgba(13,138,111,.1)', players: 2, fee: '1000' },
      { id: 3, name: '新手练习', meta: '四人 · 入门规则', icon: '📗', iconBg: 'rgba(43,168,74,.1)', players: 1, fee: '0' },
      { id: 4, name: '月下茶室', meta: '三人 · 速局模式', icon: '🌙', iconBg: 'rgba(124,107,196,.1)', players: 2, fee: '300' },
    ],
  },

  selectMode(e) {
    this.setData({ selectedMode: Number(e.currentTarget.dataset.players) });
  },

  startMatch() {
    tt.navigateTo({ url: '/pages/game/game?mode=pve' });
  },

  joinRoom(e) {
    const id = e.currentTarget.dataset.id;
    tt.navigateTo({ url: '/pages/game/game?mode=room&roomId=' + id });
  },
});
