---
type: summary
title: 前端 TS 逻辑层摘要
description: client-ts——协议类型 + tt.connectSocket 封装 + GameClient，供 Cocos Creator 接入
tags: [frontend, typescript, cocos, client]
sources: [../client-ts/]
updated: 2026-07-12
---

# 前端 TS 逻辑层（client-ts）

不依赖 Cocos 编辑器、可独立 `tsc` 编译的前端逻辑层。Cocos 项目建好后接入。

## 结构
```
client-ts/src/
├── protocol/messages.ts   # 消息类型常量 + 信封 + 各 data 类型（与后端 messages.md 对齐）
├── net/SocketClient.ts     # tt.connectSocket 封装：connect / request(seq 配对) / send / on(订阅)
├── game/GameClient.ts      # 高层 API：login / match / discard / claim + 事件订阅
└── index.ts                # 统一导出
```

## 关键设计
- **seq 自动配对**：`request()` 自增 seq，等待回填同 seq 的响应，带 10s 超时
- **广播订阅**：`on(type, handler)` 返回取消订阅函数
- **tt 最小声明**：`declare const tt` 占位（仅 `connectSocket`），TODO 换官方 minigame-api 类型
- **GameClient 作为唯一门面**：Cocos 场景脚本只与 `GameClient` 交互，不直接接触 `SocketClient`

## 验证
- `npm install` + `tsc --noEmit` ✅ 通过
- `tsc` 生成 `dist/` ✅ 正常（`index.js` + `.d.ts` + 三个子目录）
- 运行时联调（连后端 login 往返）待 Cocos 项目接入后做——client-ts 依赖 tt 运行时，Node 环境跑不了

## 与后端的关系
类型与 [[summary-protocol-v1]] 双向对齐，是 [[concept-player-abstraction]] 描述的 `HumanPlayer` 的客户端对端。

## 接入 Cocos（待办）
1. 用 Cocos Dashboard 建项目（3.x）
2. 将 `client-ts/src/` 拷入 `assets/scripts/`（或作为外部模块引用）
3. 场景脚本里 `import { GameClient } from '...'`，调用 `connect → login → match`
