---
type: summary
title: 目标架构设计摘要
description: 麻将游戏生产级目标架构--Spring Boot + Redis + MySQL + WebSocket，模块划分与核心流程
tags: [architecture, spring-boot, redis, mysql, websocket, cocos]
sources: [用户设计输入 2026-07-12]
updated: 2026-07-12
---

# 目标架构设计

用户提供的生产级目标架构蓝图。当前按**分阶段**推进，本页为阶段 2-3 的目标。

## 技术选型（目标）
| 层 | 方案 | 说明 |
|---|---|---|
| 前端 | Cocos Creator 3.x | 抖音官方推荐（已定） |
| 后端 | Spring Boot 3.x | 设计写 2.7+，建议升 3.x（2.7 开源已停维护，3.x 需 Java 17，现状匹配） |
| 实时 | WebSocket（jakarta.websocket） | 全双工，支持 wss |
| 缓存/状态 | Redis 6.x | 分布式共享，持久化 |
| 持久化 | MySQL 8.x InnoDB | 用户、对局历史 |
| 分布式锁 | Redis SET NX + Lua / Redisson | 房间级串行化 |
| JSON | Jackson | 序列化 |
| 构建 | Maven | |

## 模块划分
- **用户模块**：注册/登录/信息/积分
- **房间模块**：创建/加入/退出/列表/匹配
- **游戏核心**：牌墙管理、牌局状态机、动作仲裁、胡牌判定与番数
- **结算模块**：积分/流水/对局存档
- **通知模块**：WebSocket 推送（事件驱动）
- **定时任务**：清理超时房间/数据备份

## 架构图
```
抖音客户端(Cocos) ↔ WebSocket+HTTPS ↔ Nginx ↔ Spring Boot 集群(多节点) ↔ Redis(缓存/状态)+MySQL(持久化)
```

## 核心流程
1. **房间生命周期**：创建(6位 room_id，存 MySQL+Redis)->加入(满4)->开始->进行->结束->定时清理
2. **牌局状态机**：`WAITING -> PLAYING -> WAITING_ACTION -> SETTLED`
3. **动作仲裁（核心）**：出牌后进 WAITING_ACTION + 超时定时器(3-5s)；HU 请求立即终止定时器并结算；超时无 HU 则按序执行首个碰/杠，否则下家摸牌。挂起用 `pending` 字段存意向。
4. **洗牌发牌**：Fisher-Yates + SecureRandom；每人 13（庄 14）；剩余存 Redis List `wall`
5. **胡牌判定**：递归回溯，4 面子(顺子/刻子)+1 将；七对特例；**服务端执行**，客户端仅提交意愿

## 已定决策（2026-07-12）
1. **前端**：回到 Cocos Creator 3.x（原生 TS 的 UI 工作废弃，逻辑层 messages/SocketClient/GameClient 可迁入 Cocos）
2. **后端**：分阶段--阶段1 单机可玩(Netty 核心)，阶段2 上 Spring Boot + Redis/MySQL
3. **规则**：四川血战（108 张，缺一门，流局算分）

## 推回记录
- Spring Boot 2.7+ -> 3.x（版本时效）
- 分布式组件（Redis/MySQL/Nginx/Redisson/多节点）阶段2 再引入，避免骨架阶段过早陷基础设施

## 关联
- 协议：[[summary-protocol-v1]]
- 规则：[[concept-rule-sichuan]]（待写）
- Player 抽象：[[concept-player-abstraction]]
- 当前骨架：[[summary-frontend]]（将随 Cocos 迁移调整）
