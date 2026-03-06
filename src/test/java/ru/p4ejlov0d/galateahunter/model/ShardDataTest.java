package ru.p4ejlov0d.galateahunter.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
class ShardDataTest {
    @Test
    void sellSummary() {
        final ShardData.Summary expected = new ShardData.Summary(10, 100.05D);
        final List<ShardData.Summary> sellSummary = new ArrayList<>(List.of(new ShardData.Summary(100, 0.009D), new ShardData.Summary(10, 100.05D), new ShardData.Summary(5, 100.04D)));
        final ShardData shardData = new ShardData(sellSummary, null);
        final ShardData.Summary first = shardData.sellSummary().getFirst();

        assertEquals(expected, first);
    }

    @Test
    void buySummary() {
        final ShardData.Summary expected = new ShardData.Summary(100, 0.009D);
        final List<ShardData.Summary> buySummary = new ArrayList<>(List.of(new ShardData.Summary(10, 100.05D), new ShardData.Summary(100, 0.009D), new ShardData.Summary(5, 100.04D)));
        final ShardData shardData = new ShardData(null, buySummary);
        final ShardData.Summary first = shardData.buySummary().getFirst();

        assertEquals(expected, first);
    }

    @Test
    void purchaseTo() {
        final List<ShardData.Summary> buySummary = new ArrayList<>(List.of(new ShardData.Summary(10, 100.05D), new ShardData.Summary(100, 0.009D), new ShardData.Summary(5, 100.04D)));
        final ShardData shardData = new ShardData(null, buySummary);

        shardData.purchaseTo(100, new ShardData.Summary(10, 100D));

        // nothing changed
        assertEquals(10, shardData.buySummary().getLast().amount());

        shardData.purchaseTo(9, shardData.buySummary().getLast());

        assertEquals(new ShardData.Summary(1, 100.05D), shardData.buySummary().getLast());
        assertEquals(1, shardData.buySummary().size());
    }

    @Test
    void copy() {
        final List<ShardData.Summary> buySummary = new ArrayList<>(List.of(new ShardData.Summary(0, 0D)));
        final List<ShardData.Summary> sellSummary = new ArrayList<>();
        final ShardData shardData = new ShardData(sellSummary, buySummary);
        final ShardData copy = shardData.copy();

        assertNotSame(shardData, copy);
        assertNotSame(buySummary, copy.buySummary());
        assertNotSame(sellSummary, copy.sellSummary());
        assertArrayEquals(sellSummary.toArray(), copy.sellSummary().toArray());
        assertArrayEquals(buySummary.toArray(), copy.buySummary().toArray());
    }
}