# Wiki 时间线日志

> 追加式日志。事件前缀格式 `## [YYYY-MM-DD] <type> | <title>`，便于 `grep "^## \[" log.md | tail` 查看最近活动。
> type 取值：`init` / `ingest` / `lint` / `update`

---

## [2026-07-11] init | 初始化 Wiki 知识库

- 在项目内建立 wiki，wiki 根 = `D:\vis\douyin\majiang\wiki`（in-project subdirectory 模式）
- 创建骨架文件：`wiki/index.md`、`wiki/log.md`、`wiki/overview.md`
- 当前项目状态：抖音原生小游戏空白模板（`majiang/game.js` 仅 36 行官方模板代码），尚无实际麻将逻辑
- 技术选型方向（讨论中，未最终确认）：
  - 前端引擎：倾向 [[concept-douyin-minigame]] 平台下的 Cocos Creator
  - 后端：Java + Netty（WebSocket 实时通信）
- 待办：graph.md 待页面达到 ≥ 5 个后生成

## [2026-07-11] update | Wiki 目录结构调整

- 由 "project as wiki"（wiki 文件散在项目根）改为 "in-project subdirectory"（统一收纳进 `wiki/`）
- wiki 根现为 `D:\vis\douyin\majiang\wiki`，与代码目录 `majiang/` 并列
- 原始材料目录 `raw/` 待首次 ingest 时创建

## [2026-07-12] ingest | 搭建后端骨架 + 通信协议

- 新增页面：
  - [[summary-protocol-v1]]：前后端通信协议 v1（信封 + 13 类消息）
  - [[concept-player-abstraction]]：Player 统一抽象设计决策（人/AI 复用牌桌逻辑）
- 项目产出：
  - `protocol/messages.md`：完整协议契约（双端共享，未搬入 raw/，作为 source 引用）
  - `server/`：Java 17 + Netty 4.1.133 后端骨架（12 源文件，编译通过、WebSocket 监听 9000/ws 验证通过）
    - 分层：`netty`（WebSocketServer/GameFrameHandler）/ `message`（Message/MessageType）/ `game`（Player/Game/MahjongRule/DefaultMahjongRule）/ `player`（HumanPlayer/AiPlayer）/ `room`（RoomManager）
- 技术栈确认：Cocos Creator 3.x（前端，待建项目）+ Java/Netty（后端，✅ 骨架完成）
- 决策：人机对战（1真人+3AI）与联网对战复用同一套 Game，靠 `Player` 接口抽象
- 下一步：前端 `client-ts/` 层 + 麻将规则选型

## [2026-07-12] ingest | 搭建前端 TS 逻辑层（client-ts，后已并入 majiang）

- 新增页面：[[summary-client-ts]]（前端 TS 逻辑层）
- 项目产出 `client-ts/`：messages.ts / SocketClient.ts / GameClient.ts，tsc 编译通过
- 边界说明：Cocos Creator 场景/UI 骨架需编辑器创建
- ⚠️ 本条所述 `client-ts/` 在后续 update 中已并入 `majiang/src/`，页面改名为 [[summary-frontend]]

## [2026-07-12] update | 前端改用原生小游戏 + TS（弃 Cocos，并入 client-ts）

- 决策：前端不使用 Cocos Creator，改用 `majiang/` 原生抖音小游戏项目 + TypeScript
  - 原因：用户选择直接在原生项目开发，无需 Cocos 编辑器，界面与逻辑可在此编写
- 结构变更：
  - `client-ts/src/` 三个文件（messages/SocketClient/GameClient）迁入 `majiang/src/{protocol,net,game}/`，`client-ts/` 目录删除
  - `majiang/` 加 `tsconfig.json`（CommonJS, outDir=js, rootDir=src, strict, lib ES2017+DOM）、`package.json`
  - 新增 `majiang/src/main.ts`（应用入口：画布 + 连接 + 登录 + 匹配）、`src/scene/board.ts`（牌桌绘制骨架）、`src/types/tt.d.ts`（tt 全局声明）
  - `game.js` 改为 `require('./js/main')`，原模板代码替换
- 验证：`npm install` + `tsc --noEmit` ✅；`tsc` 生成 `js/` 产物 ✅
- wiki：`summary-client-ts.md` 改名为 [[summary-frontend]]（内容更新为 majiang/src 说明），overview/index 同步刷新
- 待办：前后端联调（抖音开发者工具实跑）、麻将规则选型、牌桌 UI 细化

## [2026-07-12] ingest | 目标架构设计 + 三项关键决策

- 新增 [[summary-architecture]]：用户提供的目标架构（Spring Boot + Redis + MySQL + WebSocket，含模块/状态机/动作仲裁/胡牌判定）
- 三项决策（用户拍板）：
  1. 前端：回到 Cocos Creator 3.x（原生 TS 的 UI 工作废弃，逻辑层 messages/SocketClient/GameClient 可迁入 Cocos）
  2. 后端：分阶段--阶段1 单机可玩(Netty 核心)，阶段2 上 Spring Boot + Redis/MySQL
  3. 规则：四川血战（108 张，缺一门）
- 推回：Spring Boot 2.7+ 建议升 3.x；分布式组件阶段2 再引入
- 下一步：阶段1 实现游戏核心（牌墙/状态机/动作仲裁/胡牌判定），纯后端不依赖前端

## [2026-07-12] update | 日麻核心可玩完成

- 规则切换：四川血战(108 张) -> 日麻(136 张，W/T/D 数牌 + J1-7 字牌)
- `RiichiRule`：胡牌判定(4面子1将/七对/国士/九莲/绿一色)、12 役(立直/门前清自摸/断幺九/平和近似/对对和/七对/混一色/清一色/字一色/国士/九莲/绿一色)、点数(符 30/七对 25 + 番对应基本点 + 满贯及以上档位 + 庄家 3B/子家 4B)
- `MahjongRule` 接口加 `findYaku`/`score` 默认方法 + `Score` record(han,fu,basePoints,totalPoints,yaku)
- `Game`：清理四川血战遗留 `missingSuits`；加 `riichi[]` 状态 + `claim("riichi")` 声明；`settleWin` 算分并广播 han/fu/points/yaku
- 验证：`MahjongTest` 17/17 pass（役 + 点数）；`GameRunTest` 4 AI 跑通一局 SETTLED
- 待补：杠/碰仲裁、一发/里宝牌/宝牌、符精确计算、平和严格结构校验

## [2026-07-12] update | 后端规则加固（立直强制 + 振听 + 必胡集成测试）

- 立直强制：`drawnTile[seat]` 记录刚摸的牌；`riichi[]` 后 `discard` 只能出该牌，否则 `RIICHI_LOCK` 错误
- 振听（舍牌振听）：`discards[seat]` 记录每座弃牌；`claim hu` 荣和时若点炮牌在自己弃牌里则 `FURITEN` 拒绝
- 必胡集成测试 `GameSettleTest`：反射设可控手牌 + `CapturePlayer` 捕获消息，验证 `settleWin -> rule.score -> gameOver` 全链路（4 座均收到 winner/han/fu/points/yaku）
- 验证：`MahjongTest` 17/17、`GameSettleTest` 1/1、`GameRunTest` 4 AI 跑通 SETTLED
