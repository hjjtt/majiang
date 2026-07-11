---
type: overview
title: 抖音麻将小游戏项目综述
description: 项目整体状态、技术选型方向与待定问题
tags: [douyin, minigame, mahjong, cocos, java, netty]
sources: []
updated: 2026-07-11
---

# 项目综述

## 一句话定位
为抖音小游戏平台开发一款**麻将**对战游戏，目前处于空白模板阶段，尚未开始实际游戏逻辑开发。

## 当前状态（2026-07-11）

| 项 | 值 |
|---|---|
| 项目名 | `majiang` |
| 平台类型 | 抖音原生小游戏（`douyinProjectType: "native"`） |
| AppID | `tt36cc4071a8a9d2e702` |
| 屏幕方向 | 竖屏 `portrait` |
| 代码现状 | `majiang/game.js` 为官方空白模板（36 行），仅绘制背景文字与 `icon.png` |
| 运行时 API | `tt.*` 全局对象（`tt.getSystemInfoSync` / `tt.createCanvas` / `tt.createImage`），无 DOM |

## 目录结构
```
D:\vis\douyin\majiang\          # 项目根
├── wiki\                       # wiki 根（本文件所在）
│   ├── index.md                # wiki 目录
│   ├── log.md                  # wiki 时间线
│   ├── overview.md             # 本文件
│   └── raw\                    # 原始材料（待首次 ingest 创建）
├── __MACOSX\                   # macOS zip 残留（建议删除）
└── majiang\                    # 抖音开发者工具识别的小游戏项目
    ├── game.js                 # 入口（模板代码）
    ├── game.json               # 屏幕方向配置
    ├── project.config.json     # 开发者工具项目配置
    └── icon.png                # 应用图标
```

## 技术选型方向（讨论中）

### 前端引擎：倾向 Cocos Creator
抖音小游戏对前端引擎的选择有强约束（运行时非浏览器 DOM，基于 Canvas/WebGL）。在支持抖音小游戏导出的引擎中：

- **Cocos Creator 3.x** ✅ 首选 —— 官方适配、2D 能力强（麻将即 2D）、TypeScript、跨平台（抖音/微信/H5）
- **LayaAir** —— 国产，支持好但生态弱于 Cocos
- **Egret（白鹭）** ❌ 2022 年已停止维护，不选
- **原生 Canvas** ❌ 仅适合极简 demo，做麻将 UI/动画成本过高

详见未来页面 [[entity-cocos-creator]]、[[concept-douyin-minigame]]。

### 后端：Java + Netty
后端语言与前端的耦合点在**通信方式**，而非引擎选择：

```
前端 (Cocos + TS) ──WebSocket(tt.connectSocket)──► Java/Netty
                   └─ HTTP (tt.request) ────────────► 登录/匹配/结算
```

- **Netty** 做 WebSocket Server，Java 实时游戏服务端事实标准
- **协议**：前期 JSON 快速开发，量大后切 Protobuf
- **关键原则**：洗牌 / 发牌 / 判胡 / 防作弊逻辑**全部放服务端**，客户端不可信，只负责展示与操作上报

## 待定问题（需要你拍板）
1. **麻将规则**：四川血战 / 国标 / 推倒胡 / 地方玩法？
2. **对战模式**：人机（需 AI） / 真人联网 / 两者都要？
3. **是否最终采用 Cocos Creator**（当前倾向，未确认）
4. **美术资源**：是否已有牌面/UI 素材，还是需要从零设计？

## 下一步建议
1. 确认麻将规则与对战模式 → 据此设计服务端牌局状态机
2. 用 Cocos Creator 初始化前端项目骨架
3. 定义前后端通信协议（房间/匹配/出牌/胡牌消息格式）
