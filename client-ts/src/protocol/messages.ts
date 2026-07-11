/**
 * 通信协议类型定义。与后端 protocol/messages.md 一一对应。
 * 消息信封：{ type: string, seq: number, data? }
 */

/** 消息类型常量 */
export const MessageType = {
  LOGIN: 'login',
  LOGIN_RESULT: 'loginResult',
  MATCH: 'match',
  MATCH_RESULT: 'matchResult',
  DEAL_TILES: 'dealTiles',
  TURN_CHANGE: 'turnChange',
  DRAW_TILE: 'drawTile',
  DISCARD: 'discard',
  TILE_DISCARDED: 'tileDiscarded',
  CLAIM: 'claim',
  CLAIM_RESULT: 'claimResult',
  GAME_OVER: 'gameOver',
  ERROR: 'error',
} as const;

export type MessageTypeValue = (typeof MessageType)[keyof typeof MessageType];

/** 消息信封 */
export interface Envelope<T = unknown> {
  type: string;
  seq: number;
  data?: T;
}

/** 对战模式 */
export type MatchMode = 'pvp' | 'pve' | 'mix';

/** 声明动作类型 */
export type ClaimAction = 'chi' | 'peng' | 'gang' | 'hu';

/** 座位玩家信息 */
export interface PlayerInfo {
  seat: number;
  playerId: string;
  name: string;
  isAi: boolean;
}

// ---- 各消息 data 结构 ----

export interface LoginData {
  token: string;
}
export interface LoginResultData {
  ok: boolean;
  playerId: string;
  name: string;
  reason?: string;
}
export interface MatchData {
  mode: MatchMode;
}
export interface MatchResultData {
  roomId: string;
  seat: number;
  players?: PlayerInfo[];
  mode?: MatchMode;
}
export interface DealTilesData {
  hand: string[]; // 牌编码，占位 W1/T9/D5/WE/DR，最终编码随规则定
}
export interface TurnChangeData {
  seat: number;
  action: 'draw' | 'discard' | 'claim';
}
export interface DrawTileData {
  tile: string;
}
export interface DiscardData {
  tile: string;
}
export interface TileDiscardedData {
  seat: number;
  tile: string;
}
export interface ClaimData {
  action: ClaimAction;
  tiles: string[];
}
export interface ClaimResultData {
  ok: boolean;
  seat: number;
  action: ClaimAction;
  tiles: string[];
  reason?: string;
}
export interface GameOverData {
  winner: number;
  scores: Record<string, number>;
}
export interface ErrorData {
  code: string;
  message: string;
}
