package ru.p4ejlov0d.galateahunter.screen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import ru.p4ejlov0d.galateahunter.utils.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.config.GalateaHunterConfig;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.utils.Enums;
import ru.p4ejlov0d.galateahunter.utils.LanguageResourceHandler;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.util.List;
import java.util.Optional;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.NAME;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.VERSION;

@Environment(EnvType.CLIENT)
public class GalateaHunterScreen {
    private static final Component TITLE = Component.literal(NAME + " v" + VERSION);

    private GalateaHunterScreen() {
    }

    @Environment(EnvType.CLIENT)
    public static Screen createGui(@Nullable Screen parent) {
        return createGui(parent, null);
    }

    @Environment(EnvType.CLIENT)
    public static Screen createGui() {
        return createGui(null, null);
    }

    @Environment(EnvType.CLIENT)
    public static Screen createGui(@Nullable Screen parent, @Nullable String category) {
        final LanguageResourceHandler languageResourceHandler = LanguageResourceHandler.getInstance();
        final LanguageModel languageModel = languageResourceHandler.getLanguageModel();
        final GalateaHunterConfig config = ModConfigHolder.getConfig();

        final ConfigBuilder configBuilder = ConfigBuilder.create().setParentScreen(parent).setTitle(TITLE);
        final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

        // build empty
        if (languageModel == null) return configBuilder.build();

        entryBuilder.setResetButtonKey(Component.literal(languageModel.reset()));

        // categories
        createGeneralCategory(configBuilder, languageModel, entryBuilder, languageResourceHandler, config);
        createHuntingCategory(configBuilder, languageModel, entryBuilder, config);

        configBuilder.setSavingRunnable(ModConfigHolder::save);

        Optional.ofNullable(category).ifPresent(string -> {
            if (configBuilder.hasCategory(Component.literal(string)))
                configBuilder.setFallbackCategory(configBuilder.getOrCreateCategory(Component.literal(string)));
        });

        return configBuilder.build();
    }

    private static void createGeneralCategory(@NotNull ConfigBuilder configBuilder, @NotNull final LanguageModel languageModel, @NotNull ConfigEntryBuilder entryBuilder, @NotNull LanguageResourceHandler languageResourceHandler, @NotNull GalateaHunterConfig config) {
        ConfigCategory general = configBuilder.getOrCreateCategory(Component.literal(languageModel.generalCategory()));
        general.setDescription(LanguageModel.toTexts(languageModel.generalDescriptions()));

        final SelectionListEntry<String> changeLanguage = entryBuilder.startSelector(Component.literal(languageModel.lang()), languageResourceHandler.loadLangNames(), languageModel.langName())
                .setDefaultValue("English")
                .setSaveConsumer(lang -> {
                    LanguageManager langManager = Minecraft.getInstance().getLanguageManager();
                    if (config.languageCode == null && langManager.getLanguage(langManager.getSelected()).name().equals(lang)) {
                        return;
                    }
                    languageResourceHandler.changeLangCodeByLangName(lang);
                })
                .setTooltip(Component.literal(languageModel.languageTooltip()))
                .build();

        final BooleanListEntry beautifulBazaarCategory = entryBuilder.startBooleanToggle(Component.literal(languageModel.beautifulBazaar()), config.isBeautifulBazaarCategoryEnabled)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> config.isBeautifulBazaarCategoryEnabled = bool)
                .setYesNoTextSupplier(bool -> bool ? Component.literal(languageModel.enabled()).withColor(Colors.GREEN) : Component.literal(languageModel.disabled()).withColor(Colors.LIGHT_RED))
                .setTooltip(/*Component.literal(languageModel.beautifulBazaarTooltip())*/ Component.literal(languageModel.unsupported()))
                .setRequirement(() -> false) // unsupported
                .build();

        final BooleanListEntry resetLanguage = entryBuilder.startBooleanToggle(Component.literal(languageModel.resetLanguage()), false)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> {
                    if (bool) languageResourceHandler.changeLangCodeByLangName(null);
                })
                .setYesNoTextSupplier(bool -> bool ? Component.literal(languageModel.resetTrue()).withColor(Colors.LIGHT_RED) : Component.literal(languageModel.resetFalse()).withColor(Colors.GREEN))
                .setTooltip(Component.literal(languageModel.resetLanguageTooltip()).setStyle(Style.EMPTY.withColor(Colors.LIGHT_YELLOW).withBold(true)))
                .build();

        final BooleanListEntry resetSettings = entryBuilder.startBooleanToggle(Component.literal(languageModel.resetSettings()), false)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> {
                    if (bool) {
                        languageResourceHandler.changeLangCodeByLangName(null);
                        ModConfigHolder.reset();
                    }
                })
                .setYesNoTextSupplier(bool -> bool ? Component.literal(languageModel.resetTrue()).withStyle(style -> style.withColor(Colors.RED).withBold(true)) : Component.literal(languageModel.resetFalse()).withColor(Colors.GREEN))
                .setTooltip(Component.literal(languageModel.resetSettingsTooltip()).setStyle(Style.EMPTY.withColor(Colors.YELLOW).withBold(true)))
                .build();

        general.addEntry(changeLanguage);
        general.addEntry(beautifulBazaarCategory);
        general.addEntry(resetLanguage);
        general.addEntry(resetSettings);
    }

    private static void createHuntingCategory(@NotNull ConfigBuilder configBuilder, @NotNull final LanguageModel languageModel, @NotNull ConfigEntryBuilder entryBuilder, @NotNull GalateaHunterConfig config) {
        ConfigCategory hunting = configBuilder.getOrCreateCategory(Component.literal(languageModel.huntingCategory()));
        hunting.setDescription(LanguageModel.toTexts(languageModel.huntingDescriptions()));

        final BooleanListEntry huntingBoxToggle = entryBuilder.startBooleanToggle(Component.literal(languageModel.huntingBox()), config.tracking.isHuntingBoxEnabled)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> config.tracking.isHuntingBoxEnabled = bool)
                .setYesNoTextSupplier(bool -> bool ? Component.literal(languageModel.enabled()).withColor(Colors.GREEN) : Component.literal(languageModel.disabled()).withColor(Colors.LIGHT_RED))
                .setTooltip(/*Component.literal(languageModel.huntingBoxTooltip())*/ Component.literal(languageModel.unsupported()))
                .setRequirement(() -> false) // unsupported
                .build();

        final BooleanListEntry attributeMenuToggle = entryBuilder.startBooleanToggle(Component.literal(languageModel.attributeMenu()), config.tracking.isAttributeMenuEnabled)
                .setDefaultValue(false)
                .setSaveConsumer(bool -> config.tracking.isAttributeMenuEnabled = bool)
                .setYesNoTextSupplier(bool -> bool ? Component.literal(languageModel.enabled()).withColor(Colors.GREEN) : Component.literal(languageModel.disabled()).withColor(Colors.LIGHT_RED))
                .setTooltip(/*Component.literal(languageModel.attributeMenuTooltip())*/ Component.literal(languageModel.unsupported()))
                .setRequirement(() -> false) // unsupported
                .build();

        final SubCategoryListEntry trackingSubCategory = entryBuilder.startSubCategory(Component.literal(languageModel.tracking()), List.of(huntingBoxToggle, attributeMenuToggle))
                .setTooltip(Component.literal(languageModel.trackingTooltip()))
                .build();

        final IntegerSliderEntry crocodileLevelInput = entryBuilder.startIntSlider(Component.literal(languageModel.crocodile()), config.recipe.crocodileLevel, 0, 10)
                .setDefaultValue(0)
                .setSaveConsumer(level -> config.recipe.crocodileLevel = level)
                .build();

        final IntegerSliderEntry seaSerpentLevelInput = entryBuilder.startIntSlider(Component.literal(languageModel.seaSerpent()), config.recipe.seaSerpentLevel, 0, 10)
                .setDefaultValue(0)
                .setSaveConsumer(level -> config.recipe.seaSerpentLevel = level)
                .build();

        final IntegerSliderEntry tiamatLevelInput = entryBuilder.startIntSlider(Component.literal(languageModel.tiamat()), config.recipe.tiamatLevel, 0, 10)
                .setDefaultValue(0)
                .setSaveConsumer(level -> config.recipe.tiamatLevel = level)
                .build();

        final EnumListEntry<Enums.BazaarStrategy> bazaarStrategy = entryBuilder.startEnumSelector(Component.literal(languageModel.bazaarStrategy()), Enums.BazaarStrategy.class, config.recipe.bazaarStrategy)
                .setDefaultValue(Enums.BazaarStrategy.BUY_OFFER)
                .setSaveConsumer(strategy -> config.recipe.bazaarStrategy = strategy)
                .build();

        final SubCategoryListEntry recipeSubCategory = entryBuilder.startSubCategory(Component.literal(languageModel.recipe()), List.of(crocodileLevelInput, seaSerpentLevelInput, tiamatLevelInput, bazaarStrategy))
                .setExpanded(true)
                .setTooltip(Component.literal(languageModel.recipeTooltip()))
                .build();

        hunting.addEntry(trackingSubCategory);
        hunting.addEntry(recipeSubCategory);
    }
}
