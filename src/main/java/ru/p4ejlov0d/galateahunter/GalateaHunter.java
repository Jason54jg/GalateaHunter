package ru.p4ejlov0d.galateahunter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.p4ejlov0d.galateahunter.command.MainGuiCommand;
import ru.p4ejlov0d.galateahunter.command.RecipeCommand;
import ru.p4ejlov0d.galateahunter.utils.RemoteRepository;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;
import ru.p4ejlov0d.galateahunter.utils.registries.CommandsRegistrar;
import ru.p4ejlov0d.galateahunter.utils.registries.ResourceReloadRegistrar;

import java.nio.file.Path;

public class GalateaHunter implements ClientModInitializer {
    public static final String MOD_ID = "galateahunter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
    public static final String VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    public static final String NAME = MOD_CONTAINER.getMetadata().getName();
    public static final Path MOD_CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModConfigHolder.register();
        RemoteRepository.getInstance().checkForUpdates();

        CommandsRegistrar.register(new MainGuiCommand());
        CommandsRegistrar.register(new RecipeCommand());

        ResourceReloadRegistrar.register();
    }
}
