package com.hjjtt.majiang.game;

import java.util.List;

/**
 * 一次胡牌的计分结果。
 *
 * @param han         番数（役种番数之和）
 * @param fu          符（核心可玩版简化为固定值）
 * @param basePoints  基本点 B = min(fu * 2^(han+2), 满贯档上限)
 * @param totalPoints 赢家最终获得的总点数（已含庄家/子家、自摸/荣和分摊）
 * @param yaku        命中的役名列表
 */
public record Score(int han, int fu, int basePoints, int totalPoints, List<String> yaku) {
}
