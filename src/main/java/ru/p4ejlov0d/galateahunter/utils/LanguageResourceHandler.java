package ru.p4ejlov0d.galateahunter.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.BufferedReader;
import java.util.*;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@Environment(EnvType.CLIENT)
public class LanguageResourceHandler implements SimpleSynchronousResourceReloadListener {
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

    public String[] loadLangNames() {
        final List<String> langNamesList = new ArrayList<>();

        for (Map.Entry<String, Resource> entry : LANG_FILES.entrySet()) {
            final Resource resource = entry.getValue();
            final String resourceName = entry.getKey();

            String line;

            try (BufferedReader reader = resource.getReader()) {
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

    public void changeLangCodeByLangName(String langName) {
        final String langCode = NAME_TO_CODE.get(langName);

        currentLangCode = ModConfigHolder.getConfig().languageCode = langCode;
    }

    public @Nullable LanguageModel getLanguageModel() {
        Resource resource = Optional.ofNullable(
                LANG_FILES.get(
                        Optional.ofNullable(currentLangCode).orElseGet(() -> {
                            if (MinecraftClient.getInstance() != null) {
                                return MinecraftClient.getInstance().getLanguageManager().getLanguage();
                            }
                            return null;
                        })
                )
        ).orElse(LANG_FILES.get("en_us"));

        try (BufferedReader reader = resource.getReader()) {
            StringBuilder json = new StringBuilder();
            String s;
            ObjectMapper mapper = new ObjectMapper();

            while ((s = reader.readLine()) != null) {
                json.append(s);
            }

            return mapper.readValue(json.toString(), LanguageModel.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to deserialize object, caused by {}, language code: {}", e.getMessage(), currentLangCode);
        }

        return null;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.tryParse(MOD_ID, "load_lang");
    }

    @Override
    public void reload(@NotNull ResourceManager manager) {
        for (Identifier id : manager.findResources("lang", path -> path.toString().endsWith(".json")).keySet())
            if (id.getNamespace().equals(MOD_ID))
                LANG_FILES.put(id.getPath().split("/")[1].split("\\.")[0], manager.getResource(id).get());
    }
}
