package ru.p4ejlov0d.galateahunter.repo.impl;

import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@SuppressWarnings("ALL")
class ShardRepoImplTest {
    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void getShardImages() {
        ModConfigHolder.register();
        File[] actual = new ShardRepoImpl().getShardImages();
        File images = new File(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/images/assets/" + MOD_ID).toUri());

        assertTrue(images.exists() && actual.length > 0);
        assertArrayEquals(actual, images.listFiles());
    }

    @Test
    void getShardData() {
        File expected = new File(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/data/fusion-data.json").toUri());
        File data = new ShardRepoImpl().getShardData();

        assertNotNull(data);
        assertEquals(expected, data);
        assertTrue(data.length() > 0);
    }
}