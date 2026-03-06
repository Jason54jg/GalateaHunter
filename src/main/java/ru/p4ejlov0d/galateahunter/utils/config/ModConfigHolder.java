package ru.p4ejlov0d.galateahunter.utils.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import ru.p4ejlov0d.galateahunter.config.GalateaHunterConfig;

public class ModConfigHolder {
    private static ConfigHolder<GalateaHunterConfig> configHolder;

    public static void register() {
        AutoConfig.register(GalateaHunterConfig.class, JanksonConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(GalateaHunterConfig.class);
    }

    public static GalateaHunterConfig getConfig() {
        return configHolder.getConfig();
    }

    public static void save() {
        configHolder.save();
    }

    public static void reset() {
        final int imagesCount = getConfig().imagesCount;
        configHolder.resetToDefault();
        getConfig().imagesCount = imagesCount;
        configHolder.save();
    }
}
