package ru.p4ejlov0d.galateahunter.service;

import com.google.gson.Gson;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.repo.ShardRepo;
import ru.p4ejlov0d.galateahunter.repo.impl.ShardRepoImpl;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;
import static ru.p4ejlov0d.galateahunter.repo.impl.ShardRepoImpl.imagesRootPath;

@SuppressWarnings("unchecked")
public class ShardService extends AbstractService<ShardRepo, String, Shard> {
    public static final ShardService INSTANCE = new ShardService();

    private PackResources resourcePack;

    private ShardService() {
        super(new ShardRepoImpl());
    }

    @Override
    public CompletableFuture<Void> load() {
        return prepare().thenCompose(this::apply);
    }

    private @NotNull CompletableFuture<Map<String, Pair<String, File>>> prepare() {
        LOGGER.info("Preparing shards data");

        CompletableFuture<File[]> prepareShardImages = CompletableFuture.supplyAsync(repo::getShardImages);
        CompletableFuture<File> prepareShardData = CompletableFuture.supplyAsync(repo::getShardData);
        CompletableFuture<Map<String, Pair<String, File>>> combine = prepareShardImages.thenApply(files -> {
            final Map<String, File> idToImage = new HashMap<>();

            for (File file : files) {
                idToImage.put(file.getName().split("\\.")[0], file);
            }

            return idToImage;
        }).thenCombine(prepareShardData, (idToImage, data) -> {
            final Map<String, Pair<String, File>> jsonIdImage = new HashMap<>();

            try (BufferedReader reader = Files.newBufferedReader(data.toPath())) {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.contains("\": { \"name\": \"")) {
                        String id = line.split("\"")[1].toLowerCase();

                        jsonIdImage.put(line, Pair.of(id, Optional.ofNullable(idToImage.get(id)).orElse(new File(imagesRootPath.resolve("assets/" + MOD_ID + "/").toFile() + id + ".png"))));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error while reading shard data", e);
                jsonIdImage.clear();
            }

            return jsonIdImage;
        });

        combine.thenAccept(map -> {
            if (!map.isEmpty()) LOGGER.info("Preparing shards data complete");
            else LOGGER.warn("Preparation completed with an error");
        });

        return combine;
    }

    @Contract("_ -> new")
    private @NotNull CompletableFuture<Void> apply(@NotNull Map<String, Pair<String, File>> data) {
        return CompletableFuture.runAsync(() -> {
            if (data.isEmpty()) return;
            try {
                LOGGER.info("Ready to apply loaded shard data");

                for (Map.Entry<String, Pair<String, File>> entry : data.entrySet()) {
                    final String json = entry.getKey();
                    final String id = entry.getValue().getLeft();
                    final File image = entry.getValue().getRight();

                    final Shard shard = new Gson().fromJson(json.substring(6, json.lastIndexOf("},") == -1 ? json.length() : json.lastIndexOf("},") + 1), Shard.class);
                    Identifier texture = Identifier.fromNamespaceAndPath(MOD_ID, image.getName());

                    shard.id = id;
                    shard.texture = texture;

                    repo.getShards().put(id, shard);
                }

                final Field basePaths = resourcePack.getClass().getDeclaredField("basePaths");
                basePaths.setAccessible(true);

                final Path basePath = ((List<Path>) basePaths.get(resourcePack)).getFirst();
                basePaths.set(resourcePack, new ArrayList<>());

                final List<Path> paths = ((List<Path>) basePaths.get(resourcePack));

                paths.add(basePath);
                paths.add(imagesRootPath);

                LOGGER.info("Successfully applied shard data");
            } catch (Exception e) {
                LOGGER.warn("Failed to handle shard", e);
                repo.getShards().clear();
            }
        });
    }

    public void setResourcePack(@NotNull PackResources resourcePack) {
        this.resourcePack = resourcePack;
    }

    public @NotNull Collection<Shard> getShards() {
        return repo.getShards().values();
    }
}
