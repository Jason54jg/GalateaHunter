package ru.p4ejlov0d.galateahunter.utils.config;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.config.GalateaHunterConfig;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ModConfigHolderTest {
    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void register() {
        ModConfigHolder.register();
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toString() + "/galateahunter.json5");

        assertTrue(configFile.length() > 0);
    }

    @Test
    void getConfig() {
        assertDoesNotThrow(ModConfigHolder::getConfig);
    }

    @Test
    void save() {
        GalateaHunterConfig expected = new GalateaHunterConfig();
        expected.languageCode = "aaa";

        ModConfigHolder.getConfig().languageCode = "aaa";
        ModConfigHolder.save();
        ModConfigHolder.getConfig().languageCode = "bbb";
        AutoConfig.getConfigHolder(GalateaHunterConfig.class).load();

        assertEquals(expected.languageCode, ModConfigHolder.getConfig().languageCode);
    }

    @Test
    void reset() {
        ModConfigHolder.getConfig().languageCode = "ccc";
        ModConfigHolder.getConfig().imagesCount = 123;
        ModConfigHolder.reset();

        assertEquals(new GalateaHunterConfig().languageCode, ModConfigHolder.getConfig().languageCode);
        assertEquals(123, ModConfigHolder.getConfig().imagesCount);
    }
}