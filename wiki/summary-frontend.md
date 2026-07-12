---
type: summary
title: 前端 TS 逻辑层摘要
description: majiang/src--协议类型 + tt.connectSocket 封装 + GameClient + 场景，原生小游戏 + TS 构建
tags: [frontend, typescript, douyin, native]
sources: [../majiang/src/]
updated: 2026-07-12
---

# 前端 TS 逻辑层（majiang/src）

抖音**原生小游戏项目**（`majiang/`）+ TypeScript 开发。`tsc` 编译 `src/*.ts` 到 `js/`，`game.js` 用 `require('./js/main')` 启动。

## 结构
```
majiang/
├── game.js               # 入口：require('./js/main')
├── tsconfig.json          # CommonJS, outDir=js, rootDir=src, strict, lib ES2017+DOM
├── package.json           # build / watch / typecheck
└── src/
    ├── main.ts            # 应用入口：画布初始化 + 连接 + 登录 + 匹配
    ├── types/tt.d.ts      # declare const tt（占位，待换官方 minigame-api-typings）
    ├── protocol/messages.ts   # 消息类型 + 信封 + data 类型（与后端对齐）
    ├── net/SocketClient.ts     # tt.connectSocket 封装（request seq 配对 / on 订阅 / 超时）
    ├── game/GameClient.ts      # 高层门面（login/match/discard/claim + 事件订阅）
    └── scene/board.ts          # 牌桌 Canvas 绘制骨架
```

## 构建
```bash
cd majiang
npm install
npm run build     # tsc 编译到 js/
npm run watch     # 增量监听
```
运行：抖音开发者工具打开 `majiang/` 目录（需先 build 生成 `js/`）。

## 关键决策
- **原生 + TS 构建，而非 Cocos Creator**：用户选择直接用抖音原生小游戏项目，`tsc` 编译 TS。我之前推荐的 Cocos Creator 方案未采用（无需 Cocos 编辑器，可直接在此写界面与逻辑）。
- **CommonJS**：小游戏原生 `require`，tsconfig `module: CommonJS`
- **tt 全局声明**：`declare const tt: any` 占位（TODO 换 minigame-api-typings）
- **seq 配对 / 订阅 / 超时**：见 [[summary-protocol-v1]]

## 当前可跑流程（main.ts）
画桌面 -> `connect ws://localhost:9000/ws` -> `login('dev-token')` -> `match('pve')` -> 顶部状态条显示进度。**前后端联调需在抖音开发者工具中实跑验证**（client-ts 已并入 majiang/src，本页由原 summary-client-ts 改名而来）。

## 关联
- 协议：[[summary-protocol-v1]]
- 后端 Player 抽象的客户端对端：[[concept-player-abstraction]]
