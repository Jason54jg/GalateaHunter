package ru.p4ejlov0d.galateahunter.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.model.ShardData;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
class BazaarServiceTest {
    private static final BazaarService bazaarService = BazaarService.INSTANCE;

    @BeforeAll
    static void beforeAll() {
        TestUtils.loadShards();
        while (!bazaarService.isUpdated) {
        }
    }

    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void load() {
        bazaarService.load().thenRun(() -> {
            try {
                Field shardPrices = bazaarService.getRepo().getClass().getDeclaredField("shardPrices");
                shardPrices.setAccessible(true);

                final int size = ((Map<Shard, ShardData>) shardPrices.get(bazaarService.getRepo())).size();

                assertTrue(size == 0 || size >= 181);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertTrue(bazaarService.isUpdated);
        }).join();
    }

    @Test
    void getPrice() {
        assertEquals(Long.MAX_VALUE / 2, bazaarService.getPrice(null, 1));
        assertNotEquals(Long.MAX_VALUE / 2, bazaarService.getPrice(ShardService.INSTANCE.get("c1"), 1));
    }

    @Test
    void purchase() {
        final Shard c1 = ShardService.INSTANCE.get("c1");
        final ShardData shardData = bazaarService.get(c1);

        shardData.buySummary().add(new ShardData.Summary(10, 80.4D));

        bazaarService.purchase(c1, Integer.MAX_VALUE);
        assertTrue(shardData.buySummary().isEmpty());
    }

    @Test
    void get() {
        final var chameleon = ShardService.INSTANCE.get("l4");
        final ShardData repoData = bazaarService.getRepo().get(chameleon);
        final ShardData serviceData = bazaarService.get(chameleon);

        assertNotSame(repoData, serviceData);
        assertNotNull(serviceData);
        assertArrayEquals(repoData.buySummary().toArray(), serviceData.buySummary().toArray());
        assertArrayEquals(repoData.sellSummary().toArray(), serviceData.sellSummary().toArray());
    }

    @Test
    void restoreShardPrices() {
        bazaarService.get(ShardService.INSTANCE.get("c1"));

        try {
            Field mutableShardPrices = bazaarService.getClass().getDeclaredField("mutableShardPrices");
            mutableShardPrices.setAccessible(true);

            final Map<Shard, ShardData> map = ((Map<Shard, ShardData>) mutableShardPrices.get(bazaarService));

            assertTrue(!map.isEmpty());
            bazaarService.restoreShardPrices();
            assertTrue(map.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}