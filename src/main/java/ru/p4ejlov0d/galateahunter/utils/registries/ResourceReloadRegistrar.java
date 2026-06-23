package ru.p4ejlov0d.galateahunter.utils.registries;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import ru.p4ejlov0d.galateahunter.utils.LanguageResourceHandler;
import ru.p4ejlov0d.galateahunter.utils.ShardsReloader;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

public class ResourceReloadRegistrar {
    @SuppressWarnings("DataFlowIssue")
    public static void register() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.tryBuild(MOD_ID, "load_lang"), LanguageResourceHandler.getInstance());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.tryBuild(MOD_ID, "load_shard_data"), new ShardsReloader());
    }
}
