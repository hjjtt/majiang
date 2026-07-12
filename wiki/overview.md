---
type: overview
title: 抖音麻将小游戏项目综述
description: 项目整体状态、技术选型、架构与下一步方向
tags: [douyin, minigame, mahjong, java, netty, typescript]
sources: []
updated: 2026-07-12
---

# 项目综述

## 一句话定位
为抖音小游戏平台开发一款**麻将**对战游戏，支持联网真人对战与人机对战。后端骨架、通信协议、前端（原生小游戏 + TS）均已搭建完成，待前后端联调。

## 当前状态（2026-07-12）

| 项 | 值 |
|---|---|
| 项目名 | `majiang` |
| 平台 | 抖音原生小游戏（`douyinProjectType: "native"`），竖屏 |
| AppID | `tt36cc4071a8a9d2e702` |
| 仓库 | https://github.com/hjjtt/majiang |
| 后端 | ✅ 骨架完成（Java 17 + Netty 4.1.133 + Maven），编译通过、WebSocket 监听 `9000/ws` |
| 协议 | ✅ v1 定义完成（见 [[summary-protocol-v1]]） |
| 前端 | ✅ 原生小游戏 + TS 完成（`majiang/src/`，`tsc` 编译通过，见 [[summary-frontend]]） |
| 麻将规则 | ✅ 日麻核心可玩（`RiichiRule`：136 张、胡牌判定、12 役、立直、点数；MahjongTest 17/17、GameRunTest 跑通一局） |

## 目录结构
```
D:\vis\douyin\majiang\
├── wiki\                       # 知识库
├── protocol\                   # 前后端通信契约
│   └── messages.md
├── server\                     # 后端（Java/Netty/Maven）
│   ├── pom.xml
│   └── src/main/java/com/hjjtt/majiang/{netty,message,game,player,room}
└── majiang\                    # 前端（抖音原生小游戏 + TS）
    ├── game.js                 # 入口：require('./js/main')
    ├── tsconfig.json           # CommonJS, outDir=js, strict
    ├── package.json            # build / watch
    └── src/
        ├── main.ts             # 应用入口（画布 + 连接 + 登录 + 匹配）
        ├── protocol/messages.ts
        ├── net/SocketClient.ts
        ├── game/GameClient.ts
        └── scene/board.ts       # 牌桌 Canvas 绘制骨架
```

## 架构核心

### 1. 服务端权威
洗牌、发牌、判胡、计分、防作弊全部在 Java 后端。客户端只展示与上报操作，任何上报都经服务端校验。

### 2. Player 统一抽象（人/AI 复用）
`Player` 接口统一真人与 AI：`HumanPlayer` 走 WebSocket，`AiPlayer` 是进程内 Bot。牌桌 `Game` 对两者透明--**人机 = 1 真人 + 3 AI，联网 = 4 真人，复用同一套牌桌逻辑**。详见 [[concept-player-abstraction]]。

### 3. 通信
WebSocket（前端 `tt.connectSocket` ↔ 后端 Netty）+ JSON 信封，13 类消息见 [[summary-protocol-v1]]；前端封装见 [[summary-frontend]]。

## 技术栈
| 层 | 选型 |
|---|---|
| 前端 | 抖音原生小游戏 + TypeScript（`tsc` 编译 CommonJS） |
| 后端 | Java 17 + Netty 4.1.133 + Jackson + Logback |
| 构建 | Maven（后端）/ tsc（前端） |
| 协议 | WebSocket + JSON（量大后切 Protobuf） |

> 注：早期曾倾向 Cocos Creator，最终采用原生小游戏 + TS 方案（无需 Cocos 编辑器，界面与逻辑均在此项目编写）。

## 待定 / 下一步
1. **前后端联调**：抖音开发者工具打开 `majiang/` 实跑，验证 connect/login/match 往返（client-ts 已并入，端到端尚未实测）
2. **麻将规则**：✅ 已定日麻（`RiichiRule`），核心可玩完成；待补：杠/碰仲裁、一发/里宝牌/宝牌、符精确计算、平和严格结构校验
3. **牌编码**：✅ 已定义（W/T/D 数牌 1-9 + J1-7 字牌，见 [[summary-protocol-v1]]）
4. **牌桌 UI**：`scene/board.ts` 细化（座位、手牌、出牌动画）

## 运行 / 构建
```bash
# 后端
cd server && mvn compile exec:java                 # 默认端口 9000

# 前端
cd majiang && npm install && npm run build         # tsc -> js/
             npm run watch                          # 增量编译
# 然后用抖音开发者工具打开 majiang/ 目录运行
```
