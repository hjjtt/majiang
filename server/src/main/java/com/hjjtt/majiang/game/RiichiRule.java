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
        if (isKokushi(hand)) return true;
        if (isChuuren(hand)) return true;
        if (isRyuuiisou(hand)) return true;
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

    private static final java.util.Set<String> TERMINALS_HONORS = java.util.Set.of(
            "W1","W9","T1","T9","D1","D9","J1","J2","J3","J4","J5","J6","J7");
    private static final java.util.Set<String> GREEN = java.util.Set.of("T2","T3","T4","T6","T8","J6");

    /** 国士无双：13 种幺九牌各 1 + 任 1 种重复。 */
    private boolean isKokushi(List<String> hand) {
        if (hand.size() != 14) return false;
        Map<String, Integer> cnt = new HashMap<>();
        for (String t : hand) {
            if (!TERMINALS_HONORS.contains(t)) return false;
            cnt.merge(t, 1, Integer::sum);
        }
        boolean pair = false;
        for (String term : TERMINALS_HONORS) {
            int c = cnt.getOrDefault(term, 0);
            if (c == 0) return false;
            if (c == 2) pair = true;
            if (c > 2) return false;
        }
        return pair;
    }

    /** 九莲宝灯：同花色 1112345678999 + 任一。 */
    private boolean isChuuren(List<String> hand) {
        if (hand.size() != 14) return false;
        String s = null;
        Map<Integer, Integer> cnt = new HashMap<>();
        for (String t : hand) {
            if (isHonor(t)) return false;
            if (s == null) s = suit(t);
            else if (!s.equals(suit(t))) return false;
            cnt.merge(rank(t), 1, Integer::sum);
        }
        int[] base = {0, 3, 1, 1, 1, 1, 1, 1, 1, 3};
        int extra = 0;
        for (int r = 1; r <= 9; r++) {
            int diff = cnt.getOrDefault(r, 0) - base[r];
            if (diff < 0 || diff > 1) return false;
            if (diff == 1) extra++;
        }
        return extra == 1;
    }

    /** 绿一色：全绿牌（条2,3,4,6,8 + 发）且能胡。 */
    private boolean isRyuuiisou(List<String> hand) {
        if (hand.size() != 14) return false;
        for (String t : hand) if (!GREEN.contains(t)) return false;
        return canSplit(hand);
    }

    /** 是否有至少 1 役。核心可玩版：立直/门前清自摸/断幺九/清一色/混一色/字一色/特殊形。 */
    @Override
    public boolean hasYaku(List<String> hand, boolean isRiichi, boolean isSelfDraw, boolean isClosed) {
        if (isRiichi) return true;
        if (isClosed && isSelfDraw) return true;
        if (isAllSimple(hand)) return true;
        if (isChinitsu(hand)) return true;
        if (isHonitsu(hand)) return true;
        if (isTsuiso(hand)) return true;
        if (isSevenPairs(hand) || isKokushi(hand) || isChuuren(hand) || isRyuuiisou(hand)) return true;
        return false;
    }

    /** 断幺九：无 1/9/字牌。 */
    private boolean isAllSimple(List<String> hand) {
        for (String t : hand) {
            if (isHonor(t)) return false;
            int r = rank(t);
            if (r == 1 || r == 9) return false;
        }
        return true;
    }

    /** 清一色：全同一种数牌花色。 */
    private boolean isChinitsu(List<String> hand) {
        String s = null;
        for (String t : hand) {
            if (isHonor(t)) return false;
            if (s == null) s = suit(t);
            else if (!s.equals(suit(t))) return false;
        }
        return true;
    }

    /** 混一色：一种数牌花色 + 字牌。 */
    private boolean isHonitsu(List<String> hand) {
        String s = null; boolean hasHonor = false;
        for (String t : hand) {
            if (isHonor(t)) { hasHonor = true; continue; }
            if (s == null) s = suit(t);
            else if (!s.equals(suit(t))) return false;
        }
        return hasHonor;
    }

    /** 字一色：全字牌。 */
    private boolean isTsuiso(List<String> hand) {
        for (String t : hand) if (!isHonor(t)) return false;
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
