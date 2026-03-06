package ru.p4ejlov0d.galateahunter.repo.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.FusionData;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.repo.RecipeRepo;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;

public class RecipeRepoImpl implements RecipeRepo {
    private final ShardService shardService = ShardService.INSTANCE;

    private final Map<Shard, List<FusionData>> shardRecipes = new HashMap<>();

    @Override
    public @NotNull Map<Shard, List<FusionData>> getShardRecipes() {
        if (shardRecipes.isEmpty()) {
            final Path SHARD_DATA_FILE = shardService.getRepo().getShardData().toPath();

            try (BufferedReader reader = Files.newBufferedReader(SHARD_DATA_FILE)) {
                String line;

                while (!(line = reader.readLine()).contains("\"shards\"")) {
                    // skip first 2 lines
                    if (line.contains("\"recipes\"") || line.equals("{")) continue;

                    if (line.contains("{")) {
                        final Shard s = shardService.get(line.split("\"")[1].toLowerCase());
                        final List<FusionData> recipes = new ArrayList<>();

                        int q = 0;

                        shardRecipes.put(s, recipes);

                        while (!(line = reader.readLine()).contains("}")) {
                            if (line.trim().startsWith("\"")) {
                                q = Integer.parseInt(line.split("\"")[1]);
                                continue;
                            }

                            if (line.contains("[")) {
                                final String[] ids = line.trim().split("\", \"");
                                /*
                                    ids[0] = [\"{ID}
                                    ids[1] = {ID}\"]
                                */

                                final FusionData f = new FusionData();

                                f.quantity = q;
                                f.left = shardService.get(ids[0].substring(2).toLowerCase());
                                f.right = shardService.get(ids[1].split("\"")[0].toLowerCase());

                                recipes.add(f);
                            }
                        }
                    }
                }

                // shards like chameleon
                shardRecipes.putAll(shardService.getShards().stream()
                        .filter(s -> !shardRecipes.containsKey(s))
                        .collect(Collectors.toMap(s -> s, s -> new ArrayList<>()))
                );
            } catch (Exception e) {
                LOGGER.error("Failed to parse recipes from {}", SHARD_DATA_FILE, e);
                shardRecipes.clear();
            }
        }

        return shardRecipes;
    }

    @Override
    public @Nullable List<FusionData> get(Shard key) {
        return getShardRecipes().get(key);
    }
}
