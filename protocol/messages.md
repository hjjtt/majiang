# 通信协议 v1

> 抖音麻将小游戏 前后端通信契约。前后端**共同遵守**本文档；后端在 `server/`、前端在 `client-ts/` 各自实现类型。
> 传输层：WebSocket（前端 `tt.connectSocket`，后端 Netty）。序列化：JSON。
> 版本：v1（骨架阶段，规则相关字段见各消息说明，具体取值随麻将规则确定）

---

## 1. 信封格式

所有消息都是一个 JSON 对象，统一信封：

```json
{
  "type": "<消息类型，见 §2>",
  "seq":   1,
  "data":  { ... }
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `type` | string | ✅ | 消息类型，取值见 §2 |
| `seq` | number | ✅ | 序列号。请求方自增，响应方原样回填，用于请求-响应配对 |
| `data` | object | ❌ | 消息体，结构由 `type` 决定；无负载消息可省略 |

---

## 2. 消息列表

方向标记：`C→S` 客户端发往服务端，`S→C` 服务端发往客户端。

### 2.1 登录

#### `login` (C→S)
连接建立后第一条消息。
```json
{ "type": "login", "seq": 1, "data": { "token": "<抖音登录态 token>" } }
```

#### `loginResult` (S→C)
```json
{
  "type": "loginResult", "seq": 1,
  "data": { "ok": true, "playerId": "p_1001", "name": "张三" }
}
```
失败时 `ok:false`，附带 `reason`。

### 2.2 匹配 / 房间

#### `match` (C→S)
请求匹配进入牌桌。
```json
{ "type": "match", "seq": 2, "data": { "mode": "pvp" } }
```
- `mode`: `"pvp"` 真人对战 / `"pve"` 人机对战（1 真人 + 3 AI）/ `"mix"` 混合。AI 作为实现了 Player 接口的 Bot 填入空座位。

#### `matchResult` (S→C)
```json
{
  "type": "matchResult", "seq": 2,
  "data": {
    "roomId": "r_88",
    "seat":   0,
    "players": [
      { "seat": 0, "playerId": "p_1001", "name": "张三", "isAi": false },
      { "seat": 1, "playerId": "bot_01", "name": "机器人甲", "isAi": true }
    ]
  }
}
```
- `seat`: 自己的座位号（0..3，按东风/南风/西风/北风）。

### 2.3 牌局进行

#### `dealTiles` (S→C)
开局发初始手牌（仅发给本人，其他玩家手牌不下发）。
```json
{ "type": "dealTiles", "seq": 3, "data": { "hand": ["W1","W1","W2","W3","..."] } }
```
- 牌编码：占位格式 `花色+点数`，如 `W1`=一万、`T9`=九条、`D5`=五筒、`WE`=东风、`DR`=中。**最终编码随规则确定**（见 [[concept-tile-encoding]] 待写）。

#### `turnChange` (S→C)
轮到某座位行动。
```json
{ "type": "turnChange", "seq": 4, "data": { "seat": 0, "action": "draw" } }
```
- `action`: `"draw"` 该摸牌 / `"discard"` 该出牌 / `"claim"` 可声明动作。

#### `drawTile` (S→C)
通知客户端摸到的牌（仅发给当前摸牌玩家）。
```json
{ "type": "drawTile", "seq": 5, "data": { "tile": "W4" } }
```

#### `discard` (C→S)
玩家出牌。
```json
{ "type": "discard", "seq": 6, "data": { "tile": "W4" } }
```

#### `tileDiscarded` (S→C)
广播：某座位打出了一张牌（其余三家据此决定是否碰/杠/胡）。
```json
{ "type": "tileDiscarded", "seq": 7, "data": { "seat": 0, "tile": "W4" } }
```

#### `claim` (C→S)
声明动作（吃/碰/杠/胡）。规则合法性由服务端 `MahjongRule` 判定。
```json
{ "type": "claim", "seq": 8, "data": { "action": "peng", "tiles": ["W4","W4"] } }
```
- `action`: `"riichi"` 立直 / `"chi"` 吃 / `"peng"` 碰 / `"gang"` 杠 / `"hu"` 胡。
- `tiles`: 该动作消耗/组成的牌。

#### `claimResult` (S→C)
动作结果广播给全桌。
```json
{
  "type": "claimResult", "seq": 9,
  "data": { "ok": true, "seat": 1, "action": "peng", "tiles": ["W4","W4"] }
}
```
失败（如不合法）时 `ok:false`，仅回声明方。

### 2.4 结算

#### `gameOver` (S→C)
```json
{
  "type": "gameOver", "seq": 10,
  "data": {
    "winner": 1, "selfDraw": false,
    "han": 2, "fu": 30, "points": 2000, "yaku": ["tanyao"],
    "scores": { "0": -2000, "1": 2000, "2": 0, "3": 0 }
  }
}
```
- `winner`: 胜者座位（`-1` 流局）。
- `selfDraw`: 是否自摸（`false` = 荣和）。
- `han` / `fu` / `points` / `yaku`: 番 / 符 / 赢家总点数 / 役名列表（流局或缺省时省略）。
- `scores`: 各家点数变化（待前后端联调时对齐分摊细节）。

### 2.5 错误

#### `error` (S→C)
通用错误回复。
```json
{ "type": "error", "seq": 0, "data": { "code": "NOT_YOUR_TURN", "message": "还没轮到你" } }
```

---

## 3. 设计说明

1. **服务端权威**：洗牌、发牌、轮次、判胡、计分、防作弊全部在服务端。客户端只展示与上报操作，任何客户端上报都要经服务端校验。
2. **seq 的作用**：仅用于请求-响应配对（如 `login`↔`loginResult`）。广播类消息（`tileDiscarded`/`turnChange` 等）的 `seq` 可用 0 或自增，客户端不强依赖。
3. **牌编码待定**：当前 `W1`/`DR` 是占位。规则确定后统一在 [[concept-tile-encoding]] 页面定义，并回填本文档。
4. **AI 接入**：AI 玩家不经过 WebSocket，直接作为进程内 `Player` 实现注入牌桌；对牌桌逻辑而言，人与 AI 接口一致（见后端 `Player` 接口）。
