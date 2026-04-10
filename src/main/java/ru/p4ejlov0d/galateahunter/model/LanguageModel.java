package ru.p4ejlov0d.galateahunter.model;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public record LanguageModel(
        @SerializedName("galateahunter.general_category") String generalCategory,
        @SerializedName("galateahunter.lang") String lang,
        @SerializedName("galateahunter.lang_name") String langName,
        @SerializedName("galateahunter.language_tooltip") String languageTooltip,
        @SerializedName("galateahunter.reset") String reset,
        @SerializedName("galateahunter.hunting_category") String huntingCategory,
        @SerializedName("galateahunter.general_descriptions") String[] generalDescriptions,
        @SerializedName("galateahunter.hunting_descriptions") String[] huntingDescriptions,
        @SerializedName("galateahunter.tracking") String tracking,
        @SerializedName("galateahunter.tracking_tooltip") String trackingTooltip,
        @SerializedName("galateahunter.hunting_box") String huntingBox,
        @SerializedName("galateahunter.hunting_box_tooltip") String huntingBoxTooltip,
        @SerializedName("galateahunter.attribute_menu") String attributeMenu,
        @SerializedName("galateahunter.attribute_menu_tooltip") String attributeMenuTooltip,
        @SerializedName("galateahunter.enabled_bool") String enabled,
        @SerializedName("galateahunter.disabled_bool") String disabled,
        @SerializedName("galateahunter.beautiful_bazaar") String beautifulBazaar,
        @SerializedName("galateahunter.beautiful_bazaar_tooltip") String beautifulBazaarTooltip,
        @SerializedName("galateahunter.reset_language") String resetLanguage,
        @SerializedName("galateahunter.reset_language_tooltip") String resetLanguageTooltip,
        @SerializedName("galateahunter.reset_true") String resetTrue,
        @SerializedName("galateahunter.reset_false") String resetFalse,
        @SerializedName("galateahunter.reset_settings") String resetSettings,
        @SerializedName("galateahunter.reset_settings_tooltip") String resetSettingsTooltip,
        @SerializedName("galateahunter.recipe") String recipe,
        @SerializedName("galateahunter.recipe_tooltip") String recipeTooltip,
        @SerializedName("galateahunter.search") String search,
        @SerializedName("galateahunter.overview") String overview,
        @SerializedName("galateahunter.total_shards") String totalShards,
        @SerializedName("galateahunter.total_fusions") String totalFusions,
        @SerializedName("galateahunter.total_reptiles") String totalReptiles,
        @SerializedName("galateahunter.total_coins") String totalCoins,
        @SerializedName("galateahunter.coins_saved") String coinsSaved,
        @SerializedName("galateahunter.crocodile") String crocodile,
        @SerializedName("galateahunter.sea_serpent") String seaSerpent,
        @SerializedName("galateahunter.tiamat") String tiamat,
        @SerializedName("galateahunter.bazaar_strategy") String bazaarStrategy,
        @SerializedName("galateahunter.refresh_prices_tooltip") String refreshPricesTooltip,
        @SerializedName("galateahunter.fusions") String fusions,
        @SerializedName("galateahunter.quantity") String quantity,
        @SerializedName("galateahunter.buy_cost") String buyCost,
        @SerializedName("galateahunter.fusion_cost") String fusionCost,
        @SerializedName("galateahunter.minimize") String minimize,
        @SerializedName("galateahunter.maximize") String maximize
) {
    public static @NotNull Text @NotNull [] toTexts(@NotNull String... descriptions) {
        return Arrays.stream(descriptions).map(Text::literal).toArray(Text[]::new);
    }
}