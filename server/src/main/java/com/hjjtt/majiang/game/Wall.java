package com.hjjtt.majiang.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 牌墙：日麻 136 张。
 * 数牌：万 W / 条 T / 筒 D，各 1-9，每张 4 副 = 108。
 * 字牌：J1东 J2南 J3西 J4北 J5中 J6发 J7白，各 4 副 = 28。
 * 牌编码见 protocol/messages.md。
 */
public class Wall {
    public static final int TOTAL = 136;
    private static final String[] SUITS = {"W", "T", "D"};
    private static final String[] HONORS = {"J1", "J2", "J3", "J4", "J5", "J6", "J7"};
    private static final int COPIES = 4;

    private final List<String> tiles;
    private int cursor = 0;

    public Wall() {
        this.tiles = new ArrayList<>(TOTAL);
        for (String s : SUITS) {
            for (int rank = 1; rank <= 9; rank++) {
                String tile = s + rank;
                for (int k = 0; k < COPIES; k++) tiles.add(tile);
            }
        }
        for (String h : HONORS) {
            for (int k = 0; k < COPIES; k++) tiles.add(h);
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

    public String draw() {
        if (cursor >= tiles.size()) return null;
        return tiles.get(cursor++);
    }

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
