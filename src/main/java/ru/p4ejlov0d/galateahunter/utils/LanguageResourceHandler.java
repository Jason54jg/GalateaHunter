package ru.p4ejlov0d.galateahunter.utils;

import com.google.gson.Gson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@Environment(EnvType.CLIENT)
public class LanguageResourceHandler implements PreparableReloadListener {
    private static LanguageResourceHandler instance;

    private final Map<String, Resource> LANG_FILES = new HashMap<>();
    private final Map<String, String> NAME_TO_CODE = new HashMap<>();

    private String currentLangCode;

    private LanguageResourceHandler() {
    }

    public static LanguageResourceHandler getInstance() {
        if (instance == null) {
            instance = new LanguageResourceHandler();
            instance.currentLangCode = ModConfigHolder.getConfig().languageCode;
        }
        return instance;
    }

    public @NotNull String[] loadLangNames() {
        final List<String> langNamesList = new ArrayList<>();

        for (Map.Entry<String, Resource> entry : LANG_FILES.entrySet()) {
            final Resource resource = entry.getValue();
            final String resourceName = entry.getKey();

            String line;

            try (BufferedReader reader = resource.openAsReader()) {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("galateahunter.lang_name")) {

                        final String langName = line.split("\"")[3];

                        langNamesList.add(langName);
                        NAME_TO_CODE.put(langName, resourceName);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read line in {}", resourceName);
            }
        }

        return langNamesList.toArray(new String[LANG_FILES.size()]);
    }

    public void changeLangCodeByLangName(@Nullable String langName) {
        final String langCode = NAME_TO_CODE.get(langName);

        currentLangCode = ModConfigHolder.getConfig().languageCode = langCode;
    }

    public @Nullable LanguageModel getLanguageModel() {
        final Resource resource = Optional.ofNullable(LANG_FILES.get(getCurrentLangCode()))
                .orElse(LANG_FILES.get("en_us"));

        try (BufferedReader reader = resource.openAsReader()) {
            final StringBuilder json = new StringBuilder();
            String s;
            final Gson gson = new Gson();

            while ((s = reader.readLine()) != null) json.append(s);

            return gson.fromJson(json.toString(), LanguageModel.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to deserialize object, caused by {}, language code: {}", e.getMessage(), currentLangCode);
        }

        return null;
    }

    public @Nullable String getCurrentLangCode() {
        return Optional.ofNullable(currentLangCode).orElseGet(
                () -> Minecraft.getInstance() != null ? Minecraft.getInstance().getLanguageManager().getSelected() : null
        );
    }

    private void reload(@NotNull ResourceManager manager) {
        for (Identifier id : manager.listResources("lang", path -> path.toString().endsWith(".json")).keySet())
            if (id.getNamespace().equals(MOD_ID))
                LANG_FILES.put(id.getPath().split("/")[1].split("\\.")[0], manager.getResource(id).get());
    }

    @Override
    public CompletableFuture<Void> reload(SharedState state, Executor prepareExecutor, PreparationBarrier barrier, Executor applyExecutor) {
        return barrier.wait(null).thenRunAsync(() -> reload(state.resourceManager()), applyExecutor);
    }
}
