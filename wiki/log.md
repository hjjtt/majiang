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

## [2026-07-12] ingest | 搭建前端 TS 逻辑层

- 新增页面：
  - [[summary-client-ts]]：前端 TS 逻辑层（协议类型 + tt.connectSocket 封装 + GameClient）
- 项目产出 `client-ts/`：
  - `src/protocol/messages.ts`：消息类型常量 + 信封 + 各 data 类型（与后端 messages.md 对齐）
  - `src/net/SocketClient.ts`：tt.connectSocket 封装（connect / request 序列号配对 / send / on 订阅）
  - `src/game/GameClient.ts`：高层门面（login / match / discard / claim + 牌局事件订阅）
  - `package.json` + `tsconfig.json`（ES2020+DOM，strict）
- 验证：`npm install` + `tsc --noEmit` ✅ 通过；`tsc` 生成 `dist/` ✅
- 设计：seq 自动配对请求-响应；GameClient 作为 Cocos 脚本的唯一交互门面；`declare const tt` 占位
- 边界说明：Cocos Creator 场景/UI 骨架需编辑器创建，client-ts 是不依赖编辑器的逻辑层，待接入
- 知识页累计 4 个（overview/concept-player/summary-protocol/summary-client-ts），graph.md 仍待 ≥5
