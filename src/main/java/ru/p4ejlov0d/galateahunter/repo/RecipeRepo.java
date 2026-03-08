package ru.p4ejlov0d.galateahunter.repo;

import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.model.FusionData;
import ru.p4ejlov0d.galateahunter.model.Shard;

import java.util.List;
import java.util.Map;

public interface RecipeRepo extends Repository<Shard, List<FusionData>> {
    @NotNull Map<Shard, List<FusionData>> getShardRecipes();
}
