package com.hjjtt.majiang.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 四川血战规则胡牌判定。
 * 108 张（万 W / 条 T / 筒 D，无风箭）；缺门胡牌；4 面子 + 1 将 或 七对。
 */
public class SichuanBloodRule implements MahjongRule {

    @Override
    public boolean canHu(List<String> hand, String missingSuit) {
        if (hand == null || hand.size() % 3 != 2) return false;
        // 缺门检查：胡牌手牌不得含缺门花色
        if (missingSuit != null) {
            for (String t : hand) {
                if (suit(t).equals(missingSuit)) return false;
            }
        }
        if (isSevenPairs(hand)) return true;
        return canSplit(hand);
    }

    private static String suit(String tile) { return tile.substring(0, 1); }
    private static int rank(String tile) { return tile.charAt(1) - '0'; }

    /** 七对：14 张且 7 种牌各 2 张。 */
    private boolean isSevenPairs(List<String> hand) {
        if (hand.size() != 14) return false;
        Map<String, Integer> cnt = new HashMap<>();
        for (String t : hand) cnt.merge(t, 1, Integer::sum);
        if (cnt.size() != 7) return false;
        for (int c : cnt.values()) if (c != 2) return false;
        return true;
    }

    /** 标准 4 面子 + 1 将：遍历每组取一对作将，其余各组全拆面子。 */
    private boolean canSplit(List<String> hand) {
        Map<String, List<Integer>> groups = new TreeMap<>();
        for (String t : hand) {
            groups.computeIfAbsent(suit(t), k -> new ArrayList<>()).add(rank(t));
        }
        for (List<Integer> g : groups.values()) Collections.sort(g);
        for (String s : groups.keySet()) {
            List<Integer> g = groups.get(s);
            for (int i = 0; i + 1 < g.size(); i++) {
                if (g.get(i).equals(g.get(i + 1))) {
                    List<Integer> rest = new ArrayList<>(g);
                    rest.remove(i);
                    rest.remove(i);
                    if (allGroupsSplit(groups, s, rest)) return true;
                }
            }
        }
        return false;
    }
    private boolean allGroupsSplit(Map<String, List<Integer>> groups, String pairSuit, List<Integer> pairRest) {
        for (String s : groups.keySet()) {
            List<Integer> g = s.equals(pairSuit) ? pairRest : groups.get(s);
            if (!splitGroup(new ArrayList<>(g))) return false;
        }
        return true;
    }

    /** 单花色组能否全拆成顺子 / 刻子（大小为 3 的倍数）。 */
    private boolean splitGroup(List<Integer> ranks) {
        if (ranks.isEmpty()) return true;
        if (ranks.size() % 3 != 0) return false;
        // 刻子：前 3 张相同
        if (ranks.get(0).equals(ranks.get(1)) && ranks.get(1).equals(ranks.get(2))) {
            if (splitGroup(new ArrayList<>(ranks.subList(3, ranks.size())))) return true;
        }
        // 顺子：含 r, r+1, r+2
        int r = ranks.get(0);
        if (r <= 7) {
            List<Integer> rest = new ArrayList<>(ranks);
            if (rest.remove(Integer.valueOf(r)) && rest.remove(Integer.valueOf(r + 1)) && rest.remove(Integer.valueOf(r + 2))) {
                if (splitGroup(rest)) return true;
            }
        }
        return false;
    }
}
