---
type: overview
title: 抖音麻将小游戏项目综述
description: 项目整体状态、技术选型、架构与下一步方向
tags: [douyin, minigame, mahjong, cocos, java, netty]
sources: []
updated: 2026-07-12
---

# 项目综述

## 一句话定位
为抖音小游戏平台开发一款**麻将**对战游戏，支持联网真人对战与人机对战。当前后端骨架 + 通信协议已搭建完成，前端待搭。

## 当前状态（2026-07-12）

| 项 | 值 |
|---|---|
| 项目名 | `majiang` |
| 平台 | 抖音原生小游戏（`douyinProjectType: "native"`），竖屏 |
| AppID | `tt36cc4071a8a9d2e702` |
| 仓库 | https://github.com/hjjtt/majiang |
| 后端 | ✅ 骨架完成（Java 17 + Netty 4.1.133 + Maven），编译通过、WebSocket 监听 `9000/ws` |
| 协议 | ✅ v1 定义完成（见 [[summary-protocol-v1]]） |
| 前端 | ⏳ Cocos Creator 项目待建（需编辑器），TS 逻辑层待搭 |
| 麻将规则 | ⏳ 未定（骨架用 `DefaultMahjongRule` 占位，`canHu` 恒 false） |

## 目录结构
```
D:\vis\douyin\majiang\
├── wiki\                       # 知识库
├── protocol\                   # 前后端通信契约
│   └── messages.md
├── server\                     # 后端（Java/Netty/Maven）
│   ├── pom.xml
│   └── src/main/java/com/hjjtt/majiang/
│       ├── Bootstrap.java          # 入口
│       ├── netty/                  # WebSocket 服务 + 消息分发
│       ├── message/                # Message 信封 + MessageType
│       ├── game/                   # Player / Game / MahjongRule 抽象
│       ├── player/                 # HumanPlayer / AiPlayer
│       └── room/                   # RoomManager 匹配
└── majiang\                    # 抖音小游戏代码（空白模板，将被 Cocos 项目替代）
```

## 架构核心

### 1. 服务端权威
洗牌、发牌、判胡、计分、防作弊全部在 Java 后端。客户端只展示与上报操作，任何上报都经服务端校验。

### 2. Player 统一抽象（人/AI 复用）
`Player` 接口统一真人与 AI：`HumanPlayer` 走 WebSocket，`AiPlayer` 是进程内 Bot。牌桌 `Game` 对两者透明——**人机 = 1 真人 + 3 AI，联网 = 4 真人，复用同一套牌桌逻辑**。详见 [[concept-player-abstraction]]。

### 3. 通信
WebSocket（前端 `tt.connectSocket` ↔ 后端 Netty）+ JSON 信封，13 类消息见 [[summary-protocol-v1]]。

## 技术栈
| 层 | 选型 |
|---|---|
| 前端引擎 | Cocos Creator 3.x（项目待建；当前 `majiang/` 为抖音原生空白模板） |
| 前端语言 | TypeScript |
| 后端 | Java 17 + Netty 4.1.133 + Jackson + Logback |
| 构建 | Maven（`mvn compile exec:java`） |
| 协议 | WebSocket + JSON（量大后切 Protobuf） |

## 待定 / 下一步
1. **前端 TS 层**：搭 `client-ts/`（`tt.connectSocket` 封装 + 协议类型 + 状态管理），随后接入 Cocos 项目
2. **麻将规则**：四川血战 / 国标 / 推倒胡——确定后实现 `MahjongRule`（替换 `DefaultMahjongRule`）
3. **牌编码**：统一编码方案（[[concept-tile-encoding]] 待写）
4. **Cocos 项目**：用 Cocos Dashboard 建项目，导入 TS 层

## 运行
```bash
cd server
mvn compile exec:java                    # 默认端口 9000
mvn compile exec:java -Dexec.args="8080" # 指定端口
```
