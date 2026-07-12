package com.hjjtt.majiang.game;

import java.util.List;

/** 占位规则，恒返回 false。实际使用 SichuanBloodRule。 */
public class DefaultMahjongRule implements MahjongRule {
    @Override
    public boolean canHu(List<String> hand, String missingSuit) {
        return false;
    }
}
