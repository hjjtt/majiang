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
