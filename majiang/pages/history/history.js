// pages/history/history.js
Page({
  data: {
    records: [
      { id: 1, result: 'win', score: '+4800', mode: '四人 · 王座', time: '今天 14:20', han: 3, fu: 40, hanText: '3番40符' },
      { id: 2, result: 'lose', score: '-3200', mode: '四人 · 竞技', time: '今天 13:05', han: 0, fu: 0, hanText: '无役' },
      { id: 3, result: 'win', score: '+1200', mode: '人机练习', time: '昨天 22:10', han: 1, fu: 30, hanText: '1番30符' },
      { id: 4, result: 'lose', score: '-800', mode: '四人 · 竞技', time: '昨天 20:33', han: 0, fu: 0, hanText: '无役' },
    ],
  },
});
