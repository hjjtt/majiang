package com.hjjtt.majiang.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 日麻（立直麻将）胡牌判定。
 * 136 张（数牌万条筒 + 字牌东南西北中发白）。
 * 胡牌型：4面子+1将 / 七对 / 国士无双 / 九莲宝灯 / 绿一色（特殊形待补）。
 * 字牌只能刻子/将，不能顺子。
 */
public class RiichiRule implements MahjongRule {

    @Override
    public boolean canHu(List<String> hand, String missingSuit) {
        if (hand == null || hand.size() % 3 != 2) return false;
        if (isSevenPairs(hand)) return true;
        return canSplit(hand);
    }

    private static String suit(String tile) { return tile.substring(0, 1); }
    private static int rank(String tile) { return tile.charAt(1) - '0'; }
    private static boolean isHonor(String tile) { return tile.startsWith("J"); }

    private boolean isSevenPairs(List<String> hand) {
        if (hand.size() != 14) return false;
        Map<String, Integer> cnt = new HashMap<>();
        for (String t : hand) cnt.merge(t, 1, Integer::sum);
        if (cnt.size() != 7) return false;
        for (int c : cnt.values()) if (c != 2) return false;
        return true;
    }

    private boolean canSplit(List<String> hand) {
        Map<String, List<Integer>> groups = new TreeMap<>();
        for (String t : hand) {
            String key = isHonor(t) ? t : suit(t);
            int r = isHonor(t) ? Integer.parseInt(t.substring(1)) : rank(t);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }
        for (List<Integer> g : groups.values()) Collections.sort(g);
        for (String key : groups.keySet()) {
            List<Integer> g = groups.get(key);
            for (int i = 0; i + 1 < g.size(); i++) {
                if (g.get(i).equals(g.get(i + 1))) {
                    List<Integer> rest = new ArrayList<>(g);
                    rest.remove(i);
                    rest.remove(i);
                    if (allGroupsSplit(groups, key, rest)) return true;
                }
            }
        }
        return false;
    }
    private boolean allGroupsSplit(Map<String, List<Integer>> groups, String pairKey, List<Integer> pairRest) {
        for (String key : groups.keySet()) {
            List<Integer> g = key.equals(pairKey) ? pairRest : groups.get(key);
            boolean honor = key.startsWith("J");
            if (!splitGroup(new ArrayList<>(g), honor)) return false;
        }
        return true;
    }

    private boolean splitGroup(List<Integer> ranks, boolean honor) {
        if (ranks.isEmpty()) return true;
        if (ranks.size() % 3 != 0) return false;
        // 刻子（前 3 张相同）
        if (ranks.get(0).equals(ranks.get(1)) && ranks.get(1).equals(ranks.get(2))) {
            if (splitGroup(new ArrayList<>(ranks.subList(3, ranks.size())), honor)) return true;
        }
        if (honor) return false; // 字牌不能顺子
        // 顺子：含 r, r+1, r+2
        int r = ranks.get(0);
        if (r <= 7) {
            List<Integer> rest = new ArrayList<>(ranks);
            if (rest.remove(Integer.valueOf(r)) && rest.remove(Integer.valueOf(r + 1)) && rest.remove(Integer.valueOf(r + 2))) {
                if (splitGroup(rest, false)) return true;
            }
        }
        return false;
    }
}
