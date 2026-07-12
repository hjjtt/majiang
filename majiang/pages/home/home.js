// pages/home/home.js
Page({
  data: {
    coins: 12800,
    today: '',
    dailyTasks: [
      { id: 1, text: '完成 1 局对战', done: true },
      { id: 2, text: '累计吃碰杠 3 次', done: false },
      { id: 3, text: '在任意牌局中和牌', done: false },
    ],
    friends: [
      { id: 1, name: '小明', initial: '明', color: '#0d8a6f', status: 'online' },
      { id: 2, name: '阿花', initial: '花', color: '#c44569', status: 'playing' },
      { id: 3, name: '王五', initial: '王', color: '#7c6bc4', status: 'online' },
    ],
  },

  onLoad() {
    const now = new Date();
    const m = now.getMonth() + 1;
    const d = now.getDate();
    this.setData({ today: m + '月' + d + '日' });
  },

  goMatch() {
    tt.switchTab({ url: '/pages/match/match' });
  },

  goGame(e) {
    const mode = e.currentTarget.dataset.mode || 'pve';
    tt.navigateTo({ url: '/pages/game/game?mode=' + mode });
  },

  goFriends() {
    tt.switchTab({ url: '/pages/friends/friends' });
  },

  goProfile() {
    tt.switchTab({ url: '/pages/profile/profile' });
  },
});
