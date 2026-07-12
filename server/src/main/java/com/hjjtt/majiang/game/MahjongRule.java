package com.hjjtt.majiang.game;

import java.util.List;

/**
 * 麻将规则抽象。核心可玩日麻由 {@link RiichiRule} 实现。
 */
public interface MahjongRule {

    /** 能否胡牌（4面子1将 / 七对 / 国士 / 九莲 / 绿一色）。 */
    boolean canHu(List<String> hand, String missingSuit);

    /** 是否有至少 1 役（日麻胡牌需有役）。委托 {@link #findYaku}。 */
    default boolean hasYaku(List<String> hand, boolean isRiichi, boolean isSelfDraw, boolean isClosed) {
        return !findYaku(hand, isRiichi, isSelfDraw, isClosed).isEmpty();
    }

    /** 返回该手牌命中的全部役名（空表 = 无役，不可胡）。默认无役。 */
    default List<String> findYaku(List<String> hand, boolean isRiichi, boolean isSelfDraw, boolean isClosed) {
        return List.of();
    }

    /** 计分。默认返回 null 表示规则未实现计分。 */
    default Score score(List<String> hand, boolean isRiichi, boolean isSelfDraw, boolean isClosed,
                        boolean winnerIsDealer, boolean isRon) {
        return null;
    }
}
