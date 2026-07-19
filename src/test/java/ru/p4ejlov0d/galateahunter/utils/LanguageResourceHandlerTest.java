package ru.p4ejlov0d.galateahunter.utils;

import net.minecraft.server.packs.resources.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ALL")
class LanguageResourceHandlerTest {
    static LanguageResourceHandler testObject;
    static Class<?> clazz;
    static Field NAME_TO_CODE;
    static Field LANG_FILES;
    static Field currentLangCode;

    @BeforeAll
    static void setup() {
        ModConfigHolder.register();

        testObject = LanguageResourceHandler.getInstance();
        clazz = testObject.getClass();
        try {
            currentLangCode = clazz.getDeclaredField("currentLangCode");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        setupDefaultValues();
    }

    static void setupDefaultValues() {
        // set up default fields values
        try {
            LANG_FILES = clazz.getDeclaredField("LANG_FILES");
            NAME_TO_CODE = clazz.getDeclaredField("NAME_TO_CODE");

            LANG_FILES.setAccessible(true);
            NAME_TO_CODE.setAccessible(true);
            currentLangCode.setAccessible(true);

            ((HashMap) LANG_FILES.get(testObject)).put("en_us", new Resource(null, () -> new ByteArrayInputStream("{\"galateahunter.general_category\": \"General\",\n\"galateahunter.lang\": \"Language\",\n\"galateahunter.lang_name\": \"English\",\n\"galateahunter.language_tooltip\": \"Select a language\",\n\"galateahunter.reset\": \"Reset\",\n\"galateahunter.hunting_category\": \"Hunting\",\n\"galateahunter.general_descriptions\": null,\n\"galateahunter.hunting_descriptions\": null,\n\"galateahunter.tracking\": \"Tracking\",\n\"galateahunter.tracking_tooltip\": \"Toggle hunting box/attribute menu tracking\",\n\"galateahunter.hunting_box\": \"Hunting box\",\n\"galateahunter.hunting_box_tooltip\": \"Toggle hunting box tracking.Tracking includes: adding shards,\n deleting\",\n\"galateahunter.attribute_menu\": \"Attribute menu\",\n\"galateahunter.attribute_menu_tooltip\": \"Toggle attribute menu tracking.Tracking includes: disabled attributes,\n attribute level\",\n\"galateahunter.enabled_bool\": \"Enabled\",\n\"galateahunter.disabled_bool\": \"Disabled\",\n\"galateahunter.beautiful_bazaar\": \"Beautiful shard category in bazaar\",\n\"galateahunter.beautiful_bazaar_tooltip\": \"Change the shard category in the bazaar to a more beautiful appearance.Coming soon\",\n\"galateahunter.reset_language\": \"Reset language\",\n\"galateahunter.reset_language_tooltip\": \"Change the mod language to your chosen language in minecraft\",\n\"galateahunter.reset_true\": \"Will be reset\",\n\"galateahunter.reset_false\": \"Don't reset\",\n\"galateahunter.reset_settings\": \"Reset settings\",\n\"galateahunter.reset_settings_tooltip\": \"Reset mod settings to default\",\n\"galateahunter.recipe\": \"Shard recipes\",\n\"galateahunter.recipe_tooltip\": \"Change the settings that are taken into account when creating a recipe\",\n\"galateahunter.search\": \"Search for a recipe...\",\n\"galateahunter.overview\": \"Overview\",\n\"galateahunter.total_shards\": \"Shard produced\",\n\"galateahunter.total_fusions\": \"Total fusions\",\n\"galateahunter.total_reptiles\": \"Pure reptiles\",\n\"galateahunter.total_coins\": \"Total coins\",\n\"galateahunter.coins_saved\": \"Coins saved\",\n\"galateahunter.crocodile\": \"Crocodile level\",\n\"galateahunter.sea_serpent\": \"Sea Serpent level\",\n\"galateahunter.tiamat\": \"Tiamat level\",\n\"galateahunter.bazaar_strategy\": \"Assume bazaar prices strategy\",\n\"galateahunter.refresh_prices_tooltip\": \"Refresh prices\",\n\"galateahunter.fusions\": \"Fusions\",\n\"galateahunter.quantity\": \"Quantity\",\n\"galateahunter.buy_cost\": \"Buy cost\",\n\"galateahunter.fusion_cost\": \"Fusion cost\",\n\"galateahunter.minimize\": \"Minimize screen\",\n\"galateahunter.maximize\": \"Maximize screen\",\n\"galateahunter.unsupported\": \"Currently not supported\"}".getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        TestUtils.clearConfig();
    }

    @Test
    void loadLangNames() {
        String[] excepted = {"English"};

        try {
            assertEquals(excepted[0], testObject.loadLangNames()[0]);
            assertEquals("en_us", ((HashMap) NAME_TO_CODE.get(testObject)).get("English"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void changeLangCodeByLangName() {
        String excepted = "en_us";

        try {
            testObject.changeLangCodeByLangName("English");

            assertEquals(excepted, currentLangCode.get(testObject));
            assertEquals(excepted, ModConfigHolder.getConfig().languageCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getLanguageModel() {
        LanguageModel expected = new LanguageModel("General",
                "Language",
                "English",
                "Select a language",
                "Reset",
                "Hunting",
                null,
                null,
                "Tracking",
                "Toggle hunting box/attribute menu tracking",
                "Hunting box",
                "Toggle hunting box tracking.Tracking includes: adding shards, deleting",
                "Attribute menu",
                "Toggle attribute menu tracking.Tracking includes: disabled attributes, attribute level",
                "Enabled",
                "Disabled",
                "Beautiful shard category in bazaar",
                "Change the shard category in the bazaar to a more beautiful appearance.Coming soon",
                "Reset language",
                "Change the mod language to your chosen language in minecraft",
                "Will be reset",
                "Don't reset",
                "Reset settings",
                "Reset mod settings to default",
                "Shard recipes",
                "Change the settings that are taken into account when creating a recipe",
                "Search for a recipe...",
                "Overview",
                "Shard produced",
                "Total fusions",
                "Pure reptiles",
                "Total coins",
                "Coins saved",
                "Crocodile level",
                "Sea Serpent level",
                "Tiamat level",
                "Assume bazaar prices strategy",
                "Refresh prices",
                "Fusions",
                "Quantity",
                "Buy cost",
                "Fusion cost",
                "Minimize screen",
                "Maximize screen",
                "Currently not supported"
        );

        try {
            currentLangCode.set(testObject, "en_us");

            assertEquals(expected, testObject.getLanguageModel());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}