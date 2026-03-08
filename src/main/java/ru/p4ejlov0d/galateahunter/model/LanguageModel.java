package ru.p4ejlov0d.galateahunter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public record LanguageModel(
        @JsonProperty("galateahunter.general_category") String generalCategory,
        @JsonProperty("galateahunter.lang") String lang,
        @JsonProperty("galateahunter.lang_name") String langName,
        @JsonProperty("galateahunter.language_tooltip") String languageTooltip,
        @JsonProperty("galateahunter.reset") String reset,
        @JsonProperty("galateahunter.hunting_category") String huntingCategory,
        @JsonProperty("galateahunter.general_descriptions") String[] generalDescriptions,
        @JsonProperty("galateahunter.hunting_descriptions") String[] huntingDescriptions,
        @JsonProperty("galateahunter.tracking") String tracking,
        @JsonProperty("galateahunter.tracking_tooltip") String trackingTooltip,
        @JsonProperty("galateahunter.hunting_box") String huntingBox,
        @JsonProperty("galateahunter.hunting_box_tooltip") String huntingBoxTooltip,
        @JsonProperty("galateahunter.attribute_menu") String attributeMenu,
        @JsonProperty("galateahunter.attribute_menu_tooltip") String attributeMenuTooltip,
        @JsonProperty("galateahunter.enabled_bool") String enabled,
        @JsonProperty("galateahunter.disabled_bool") String disabled,
        @JsonProperty("galateahunter.beautiful_bazaar") String beautifulBazaar,
        @JsonProperty("galateahunter.beautiful_bazaar_tooltip") String beautifulBazaarTooltip,
        @JsonProperty("galateahunter.reset_language") String resetLanguage,
        @JsonProperty("galateahunter.reset_language_tooltip") String resetLanguageTooltip,
        @JsonProperty("galateahunter.reset_true") String resetTrue,
        @JsonProperty("galateahunter.reset_false") String resetFalse,
        @JsonProperty("galateahunter.reset_settings") String resetSettings,
        @JsonProperty("galateahunter.reset_settings_tooltip") String resetSettingsTooltip,
        @JsonProperty("galateahunter.recipe") String recipe,
        @JsonProperty("galateahunter.recipe_tooltip") String recipeTooltip,
        @JsonProperty("galateahunter.search") String search,
        @JsonProperty("galateahunter.overview") String overview,
        @JsonProperty("galateahunter.total_shards") String totalShards,
        @JsonProperty("galateahunter.total_fusions") String totalFusions,
        @JsonProperty("galateahunter.total_reptiles") String totalReptiles,
        @JsonProperty("galateahunter.total_coins") String totalCoins,
        @JsonProperty("galateahunter.coins_saved") String coinsSaved,
        @JsonProperty("galateahunter.crocodile") String crocodile,
        @JsonProperty("galateahunter.sea_serpent") String seaSerpent,
        @JsonProperty("galateahunter.tiamat") String tiamat,
        @JsonProperty("galateahunter.bazaar_strategy") String bazaarStrategy,
        @JsonProperty("galateahunter.refresh_prices_tooltip") String refreshPricesTooltip,
        @JsonProperty("galateahunter.fusions") String fusions,
        @JsonProperty("galateahunter.quantity") String quantity,
        @JsonProperty("galateahunter.buy_cost") String buyCost,
        @JsonProperty("galateahunter.fusion_cost") String fusionCost
) {
    public static @NotNull Text @NotNull [] toTexts(@NotNull String... descriptions) {
        return Arrays.stream(descriptions).map(Text::literal).toArray(Text[]::new);
    }
}