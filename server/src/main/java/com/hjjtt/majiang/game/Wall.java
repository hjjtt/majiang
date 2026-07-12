package com.hjjtt.majiang.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 牌墙：四川血战 108 张（万 W / 条 T / 筒 D，各 1-9，每张 4 副）。
 * 洗牌 Fisher-Yates（SecureRandom 随机源），支持发牌与摸牌。
 * 牌编码与 protocol/messages.md 一致：W1/T9/D5 等。
 */
public class Wall {
    public static final int TOTAL = 108;
    private static final String[] SUITS = {"W", "T", "D"};
    private static final int RANKS = 9;
    private static final int COPIES = 4;

    private final List<String> tiles;
    private int cursor = 0;

    public Wall() {
        this.tiles = new ArrayList<>(TOTAL);
        for (String s : SUITS) {
            for (int rank = 1; rank <= RANKS; rank++) {
                String tile = s + rank;
                for (int k = 0; k < COPIES; k++) tiles.add(tile);
            }
        }
        shuffle();
    }

    private void shuffle() {
        SecureRandom rnd = new SecureRandom();
        for (int i = tiles.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Collections.swap(tiles, i, j);
        }
    }

    /** 摸一张牌；牌墙空返回 null。 */
    public String draw() {
        if (cursor >= tiles.size()) return null;
        return tiles.get(cursor++);
    }

    /** 连续发 n 张牌。 */
    public List<String> deal(int n) {
        List<String> hand = new ArrayList<>(n);
        for (int i = 0; i < n; i++) hand.add(draw());
        return hand;
    }

    public int remaining() {
        return tiles.size() - cursor;
    }

    public boolean isEmpty() {
        return cursor >= tiles.size();
    }
}
