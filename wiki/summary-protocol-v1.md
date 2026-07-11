---
type: summary
title: 通信协议 v1 摘要
description: 抖音麻将前后端 WebSocket 通信契约——信封格式与 13 类消息
tags: [protocol, websocket, json]
sources: [../protocol/messages.md]
updated: 2026-07-12
---

# 通信协议 v1

抖音麻将前后端通信契约的提炼。完整定义见项目根 [`protocol/messages.md`](../protocol/messages.md)（原始契约，双端共同遵守）。

## 信封格式
所有消息统一：`{ "type": string, "seq": number, "data": object }`。`seq` 用于请求-响应配对，响应原样回填。

## 13 类消息
| 阶段 | 消息 | 方向 |
|---|---|---|
| 登录 | `login` / `loginResult` | C→S / S→C |
| 匹配 | `match`(mode) / `matchResult` | C→S / S→C |
| 牌局 | `dealTiles`、`turnChange`、`drawTile`、`discard`、`tileDiscarded` | 多为 S→C |
| 声明 | `claim`(chi/peng/gang/hu) / `claimResult` | C→S / S→C |
| 结算 | `gameOver` | S→C |
| 错误 | `error`(code) | S→C |

`match.mode`：`pvp` 真人 / `pve` 人机(1+3AI) / `mix` 混合。

## 关键决策
1. **服务端权威**：洗牌/发牌/判胡/计分全在服务端，客户端仅展示 + 上报
2. **AI 复用同一消息流**：AI 作为进程内 [[concept-player-abstraction|Player]] 实现，对 Game 而言与真人无异——人机与联网共用一套消息
3. **牌编码占位**：`W1`(一万) / `T9`(九条) / `D5`(五筒) / `WE`(东) / `DR`(中) 是占位，最终编码随规则定（[[concept-tile-encoding]] 待写）
4. **seq 仅配对**：广播消息（`tileDiscarded` / `turnChange`）的 seq 可忽略

## 双端实现
- **后端**：`server/.../message/Message.java` + `MessageType.java`；分发在 `netty/GameFrameHandler`
- **前端**：待建 `client-ts/.../protocol/messages.ts`
