package ru.p4ejlov0d.galateahunter.utils;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

public class ShardsReloader implements PreparableReloadListener {
    private void reload(@NotNull ResourceManager manager) {
        try (PackResources resourcePack = manager.listPacks()
                .filter(resourcePack1 -> resourcePack1.packId().equals(MOD_ID))
                .findFirst().orElseThrow()
        ) {
            ShardService.INSTANCE.setResourcePack(resourcePack);
            ShardService.INSTANCE.load();
        }
    }

    @Override
    public CompletableFuture<Void> reload(SharedState state, Executor prepareExecutor, PreparationBarrier barrier, Executor applyExecutor) {
        return barrier.wait(null).thenRunAsync(() -> reload(state.resourceManager()), applyExecutor);
    }
}
