package com.hjjtt.majiang.game;

import java.util.List;

/**
 * 麻将规则抽象（策略模式）。具体规则实现此接口。
 * 四川血战实现见 SichuanBloodRule。
 */
public interface MahjongRule {

    /**
     * 判断手牌是否构成胡牌。
     *
     * @param hand        手牌（胡牌时为 3n+2 张）
     * @param missingSuit 缺门花色（"W"/"T"/"D"）；四川血战胡牌时手牌不得含缺门花色，null 表示无缺门要求
     */
    boolean canHu(List<String> hand, String missingSuit);
}
