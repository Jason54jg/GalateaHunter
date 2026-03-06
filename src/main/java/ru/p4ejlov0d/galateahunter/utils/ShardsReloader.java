package ru.p4ejlov0d.galateahunter.utils;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

public class ShardsReloader implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.tryParse(MOD_ID, "load_shard_data");
    }

    @Override
    public void reload(@NotNull ResourceManager manager) {
        try (ResourcePack resourcePack = manager.streamResourcePacks()
                .filter(resourcePack1 -> resourcePack1.getId().equals(MOD_ID))
                .findFirst().orElseThrow()
        ) {
            ShardService.INSTANCE.setResourcePack(resourcePack);
            ShardService.INSTANCE.load();
        }
    }
}
