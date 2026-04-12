package ru.p4ejlov0d.galateahunter.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import ru.p4ejlov0d.galateahunter.utils.Enums;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@Config(name = MOD_ID)
public class GalateaHunterConfig implements ConfigData {
    public final Tracking tracking = new Tracking();

    public final Recipe recipe = new Recipe();

    @Comment("Default: null")
    public String languageCode = null;

    @Comment("Default: false")
    public boolean isBeautifulBazaarCategoryEnabled = false;

    @Comment("Default: 0")
    public int imagesCount = 0;

    public static class Tracking {
        @Comment("Default: false")
        public boolean isHuntingBoxEnabled = false;

        @Comment("Default: false")
        public boolean isAttributeMenuEnabled = false;
    }

    public static class Recipe {
        @Comment("Default: 0")
        public int crocodileLevel = 0;

        @Comment("Default: 0")
        public int seaSerpentLevel = 0;

        @Comment("Default: 0")
        public int tiamatLevel = 0;

        @Comment("Default: BUY_OFFER")
        public Enums.BazaarStrategy bazaarStrategy = Enums.BazaarStrategy.BUY_OFFER;
    }
}
