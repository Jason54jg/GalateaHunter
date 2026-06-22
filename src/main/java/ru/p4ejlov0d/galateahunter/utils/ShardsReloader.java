package ru.p4ejlov0d.galateahunter.utils;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

public class ShardsReloader implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        try (PackResources resourcePack = manager.listPacks()
                .filter(resourcePack1 -> resourcePack1.packId().equals(MOD_ID))
                .findFirst().orElseThrow()
        ) {
            ShardService.INSTANCE.setResourcePack(resourcePack);
            ShardService.INSTANCE.load();
        }
    }
}
