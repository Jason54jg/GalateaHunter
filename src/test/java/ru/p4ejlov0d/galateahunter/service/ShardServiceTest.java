package ru.p4ejlov0d.galateahunter.service;

import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@SuppressWarnings("ALL")
class ShardServiceTest {
    @BeforeAll
    static void beforeAll() {
        TestUtils.clearConfigDir();
    }

    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void load() {
        ModConfigHolder.register();

        ShardService.INSTANCE.setResourcePack(ModNioPackResources.create(MOD_ID, FabricLoader.getInstance().getModContainer(MOD_ID).get(), null, PackType.CLIENT_RESOURCES, null, false));
        ShardService.INSTANCE.load().thenRun(() -> {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File images = new File(configDir.resolve(MOD_ID + "/images/assets/" + MOD_ID).toUri());
            File dataFile = new File(configDir.resolve(MOD_ID + "/data/fusion-data.json").toUri());

            assertTrue(images.exists());
            assertTrue(dataFile.exists());
            assertTrue(!ShardService.INSTANCE.getShards().isEmpty());
        }).join();
    }
}