package ru.p4ejlov0d.galateahunter;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import org.apache.commons.io.FileUtils;
import ru.p4ejlov0d.galateahunter.service.ShardService;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@SuppressWarnings("ALL")
public class TestUtils {
    public static void clearConfig() {
        new File(FabricLoader.getInstance().getConfigDir().toString() + "/galateahunter.json5").delete();

        try {
            Field field = AutoConfig.class.getDeclaredField("holders");
            field.setAccessible(true);
            ((HashMap) field.get(null)).clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearConfigDir() {
        try {
            FileUtils.cleanDirectory(FabricLoader.getInstance().getConfigDir().toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadShards() {
        ModConfigHolder.register();
        ShardService.INSTANCE.setResourcePack(ModNioPackResources.create(MOD_ID, FabricLoader.getInstance().getModContainer(MOD_ID).get(), null, PackType.CLIENT_RESOURCES, null, false));
        ShardService.INSTANCE.load().join();
    }
}
