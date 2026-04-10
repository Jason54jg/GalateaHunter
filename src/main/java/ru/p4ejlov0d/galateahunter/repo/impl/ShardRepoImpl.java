package ru.p4ejlov0d.galateahunter.repo.impl;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.repo.ShardRepo;
import ru.p4ejlov0d.galateahunter.utils.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.*;

public class ShardRepoImpl implements ShardRepo {
    public static final Path imagesRootPath = MOD_CONFIG_DIR.resolve("images");
    public static final Path dataRootPath = MOD_CONFIG_DIR.resolve("data");

    private final RemoteRepository REMOTE_REPOSITORY = RemoteRepository.getInstance();
    // skyshards
    private final URI remoteDataPath = URI.create("https://skyshards.com/fusion-data.json");

    // initializes outside this class
    private final Map<String, Shard> SHARDS = new HashMap<>();

    @Override
    public @NotNull File[] getShardImages() {
        final File shardIcons = imagesRootPath.resolve("assets/" + MOD_ID + "/").toFile();

        if (!Files.isDirectory(shardIcons.toPath()) || REMOTE_REPOSITORY.isNeedUpdate())
            REMOTE_REPOSITORY.cloneRepoWithImages();

        final File[] icons = shardIcons.listFiles();

        if (icons == null) return new File[0];

        return Arrays.stream(icons)
                .filter(file -> file.isFile() && file.getName().endsWith(".png"))
                .toArray(File[]::new);
    }

    @Override
    public @Nullable File getShardData() {
        final File dataFile = new File(dataRootPath.resolve("fusion-data.json").toUri());

        if (!Files.exists(dataFile.toPath()) || REMOTE_REPOSITORY.isNeedUpdate()) {
            try {
                LOGGER.info("Downloading shard data to {}", dataFile.getAbsolutePath());
                FileUtils.copyURLToFile(remoteDataPath.toURL(), dataFile, 10000, 10000);
                LOGGER.info("Successfully downloaded shard data to {}", dataFile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("An error occurred while trying to download shard data", e);
                try {
                    Files.deleteIfExists(dataFile.toPath());
                } catch (IOException ex) {
                    LOGGER.error("Could not delete shard data file {}", dataFile.getAbsolutePath(), ex);
                }
                return null;
            }
        }

        return dataFile;
    }

    @Override
    public @NotNull Map<String, Shard> getShards() {
        return SHARDS;
    }

    @Override
    public @Nullable Shard get(String key) {
        return SHARDS.get(key);
    }
}
