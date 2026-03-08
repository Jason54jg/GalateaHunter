package ru.p4ejlov0d.galateahunter.repo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.Shard;

import java.io.File;
import java.util.Map;

public interface ShardRepo extends Repository<String, Shard> {
    @NotNull File[] getShardImages();

    @Nullable File getShardData();

    @NotNull Map<String, Shard> getShards();
}
