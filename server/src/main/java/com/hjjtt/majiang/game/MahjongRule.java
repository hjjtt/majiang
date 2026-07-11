package com.hjjtt.majiang.game;

import java.util.List;

/**
 * 麻将规则抽象（策略模式）。具体规则（四川血战/国标/...）实现此接口。
 * 骨架阶段只定义核心判胡接口；其余（洗牌、定缺、吃碰杠合法性、计分）随规则细化补充。
 */
public interface MahjongRule {

    /**
     * 判断给定手牌是否构成胡牌。
     *
     * @param hand 手牌（牌编码列表，编码见 protocol/messages.md §2.3）
     * @return 可胡返回 true
     */
    boolean canHu(List<String> hand);
}
