package com.hjjtt.majiang.game;

import java.util.List;

/**
 * 默认/占位规则实现。canHu 恒返回 false。
 * 待选定具体麻将规则（四川血战/国标/...）后替换为真正的实现。
 */
public class DefaultMahjongRule implements MahjongRule {

    @Override
    public boolean canHu(List<String> hand) {
        // TODO: 实现胡牌判定（牌型拆分：将 + 顺子/刻子）
        return false;
    }
}
