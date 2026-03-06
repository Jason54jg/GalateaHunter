package ru.p4ejlov0d.galateahunter.repo.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.model.ShardData;
import ru.p4ejlov0d.galateahunter.repo.BazaarRepo;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class BazaarRepoImplTest {
    @BeforeAll
    static void beforeAll() {
        TestUtils.loadShards();
    }

    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void updateShardPrices() {
        final BazaarRepo bazaarRepo = new BazaarRepoImpl();
        bazaarRepo.updateShardPrices();

        try {
            final Field shardPrices = bazaarRepo.getClass().getDeclaredField("shardPrices");
            shardPrices.setAccessible(true);

            final int size = ((Map<Shard, ShardData>) shardPrices.get(bazaarRepo)).size();

            assertTrue(size == 0 || size >= 181);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get() {
        final BazaarRepo bazaarRepo = new BazaarRepoImpl();
        bazaarRepo.updateShardPrices();

        final ShardData shardData = bazaarRepo.get(ShardService.INSTANCE.get("l4"));
        assertNotNull(shardData);
    }
}