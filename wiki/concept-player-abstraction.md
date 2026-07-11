---
type: concept
title: Player 统一抽象
description: 用 Player 接口统一真人与 AI，让人机与联网复用同一套牌桌逻辑
tags: [architecture, player, ai, design]
sources: [../server/src/main/java/com/hjjtt/majiang/game/Player.java]
updated: 2026-07-12
---

# Player 统一抽象

## 核心思想
`Player` 是一个接口，抽象"坐在牌桌前的一个参与者"。真人（通过网络）和 AI（进程内）都实现它，使牌桌 `Game` 的逻辑**对玩家类型透明**。

```
       ┌─────────────┐
Game ──┤  Player 接口  ├─ HumanPlayer  (WebSocket ↔ tt.connectSocket)
       └─────────────┘  └ AiPlayer     (进程内 Bot，决策逻辑待实现)
```

## 为什么这样设计
需求是**人机 + 联网都要**。若不抽象，会出现两套牌桌逻辑：一套跑真人 WebSocket 对战，一套跑人机本地对战——重复且易不一致。统一抽象后：
- 人机对战 = 1 个 `HumanPlayer` + 3 个 `AiPlayer` 坐一桌
- 联网对战 = 4 个 `HumanPlayer`
- `Game` 只认 `Player`，无需 `if (isAi)` 分叉

`RoomManager` 正是据此实现：`pve` 模式补 3 个 AI 入座并开局，`pvp` 模式凑满 4 真人开局（见 `room/RoomManager.java`）。

## 接口要点（`game/Player.java`）
- `getId() / getName() / isAi()`：身份
- `setSeat(int) / getSeat()`：入座
- `send(Message)`：推送消息。真人实现走 `GameFrameHandler.send(ctx, msg)` 下发 WebSocket；AI 实现可忽略或驱动决策

## 取舍
- **优点**：复用牌桌逻辑、AI 可独立测试、未来加录像回放也只是"另一种 Player"
- **代价**：AI 决策需从 `send()` 收到的消息中提取状态，不如直接共享状态对象直接——后续可让 Game 主动调用 `Player.onTurn()` 等回调，而非只靠 send 推送
- **关联**：消息契约见 [[summary-protocol-v1]]
