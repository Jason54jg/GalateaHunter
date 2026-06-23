package ru.p4ejlov0d.galateahunter.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import ru.p4ejlov0d.galateahunter.model.FusionData;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.screen.widget.EditBoxWithSuggestions;
import ru.p4ejlov0d.galateahunter.screen.widget.IconButtonWidget;
import ru.p4ejlov0d.galateahunter.screen.widget.ScalableWidget;
import ru.p4ejlov0d.galateahunter.service.BazaarService;
import ru.p4ejlov0d.galateahunter.service.RecipeService;
import ru.p4ejlov0d.galateahunter.service.ShardService;
import ru.p4ejlov0d.galateahunter.utils.LanguageResourceHandler;
import ru.p4ejlov0d.galateahunter.utils.tree.Node;
import ru.p4ejlov0d.galateahunter.utils.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;

@SuppressWarnings("SuspiciousNameCombination")
@Environment(EnvType.CLIENT)
public class RecipeScreen extends Screen {
    private static final Component TITLE = Component.literal("Recipe");
    public static boolean minimized;
    private static RecipeScreen savedRecipeScreen;

    private final BazaarService bazaarService = BazaarService.INSTANCE;
    private final ShardService shardService = ShardService.INSTANCE;
    private final RecipeService recipeService = RecipeService.getInstance();

    private LanguageModel languageModel;
    private String searchText;
    private Tree<Shard, FusionData> tree;

    private EditBoxWithSuggestions<Shard> search;
    private IconButtonWidget close;
    private IconButtonWidget list;
    private EditBox quantityField;
    private ScalableWidget recipeWidget;
    private IconButtonWidget minimize;

    public RecipeScreen() {
        super(TITLE);
    }

    public RecipeScreen(@Nullable String searchText) {
        this();
        this.searchText = searchText;
    }

    public static ScalableWidget getSavedRecipeWidget() {
        return savedRecipeScreen.recipeWidget;
    }

    public static RecipeScreen restoreScreen() {
        savedRecipeScreen.width = savedRecipeScreen.minecraft.getWindow().getGuiScaledWidth();
        savedRecipeScreen.height = savedRecipeScreen.minecraft.getWindow().getGuiScaledHeight();
        savedRecipeScreen.search.setRectangle((int) (savedRecipeScreen.width / 2.5d), 30, (int) (savedRecipeScreen.width / 3.3333333d), 5);
        savedRecipeScreen.quantityField.setValue(savedRecipeScreen.quantityField.getValue());
        savedRecipeScreen.quantityField.setWidth(savedRecipeScreen.width / 4 - 34);
        savedRecipeScreen.recipeWidget.setPosition(savedRecipeScreen.list.visible ? 5 : savedRecipeScreen.width / 4 + 10, 45);
        savedRecipeScreen.recipeWidget.setWidth(savedRecipeScreen.list.visible ? savedRecipeScreen.width - 10 : savedRecipeScreen.width - (savedRecipeScreen.width / 4 + 15));
        savedRecipeScreen.recipeWidget.setHeight(savedRecipeScreen.height - 50);
        savedRecipeScreen.recipeWidget.setDrawsBackground(true);
        savedRecipeScreen.minimize.setRectangle(20, 20, savedRecipeScreen.recipeWidget.getX() + savedRecipeScreen.recipeWidget.getWidth() - 30, savedRecipeScreen.recipeWidget.getY() + savedRecipeScreen.recipeWidget.getHeight() - 90);

        return savedRecipeScreen;
    }

    @Override
    protected void init() {
        languageModel = LanguageResourceHandler.getInstance().getLanguageModel();

        final Identifier normalSearchTexture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/search-field.png");
        final Identifier focusedSearchTexture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/search-field-highlighted.png");

        search = new EditBoxWithSuggestions.Builder<Shard>()
                .normalTexture(normalSearchTexture)
                .focusedTexture(focusedSearchTexture)
                .x((int) (width / 3.3333333d))
                .y(5)
                .width((int) (width / 2.5d))
                .height(30)
                .placeholder(Component.literal(languageModel.search()).withStyle(style -> style.withoutShadow().withColor(0xFFFFFFFF)))
                .toTextureFunction(shard -> shard.texture)
                .toSuggestionRender(shard -> (context, x, y, width, height, mouseX, mouseY, deltaTicks) -> {
                    final WidgetSprites textures = new WidgetSprites(Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/" + shard.rarity + ".png"), Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/" + shard.rarity + "-selected.png"));
                    final boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

                    context.blit(RenderPipelines.GUI_TEXTURED, textures.get(true, hovered), x, y, 0f, 0f, width, height, width, height);
                    context.blit(RenderPipelines.GUI_TEXTURED, shard.texture, x + 4, y + 2, 0f, 0f, height - 6, height - 6, height - 6, height - 6);
                    context.text(font, Component.literal(shard.name).withColor(getColorByRarity(shard.rarity)).append(Component.literal(" (" + shard.id.toUpperCase() + ")").withColor(0xFF808080)), x + height + 2, y + height - height * 85 / 100, 0xFFFFFFFF, false);
                    context.text(font, shard.family, x + height + 2, y + height * 62 / 100, 0xFFFFFFFF, false);
                })
                .toStringFunction(shard -> shard.name)
                .build(font, shardService.getShards());

        if (searchText != null) {
            search.insertText(searchText);
            this.setFocused(search);
        }

        final IconButtonWidget settings = new IconButtonWidget(width - 30, 10, 20, 20,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/settings.png"),
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/settings-highlighted.png"),
                btn -> minecraft.gui.setScreen(GalateaHunterScreen.createGui(this, languageModel.huntingCategory()))
        );

        final IconButtonWidget update = new IconButtonWidget(width - 55, 10, 20, 20,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/refresh.png"),
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/refresh-highlighted.png"),
                btn -> bazaarService.load()
        );

        update.setTooltip(Tooltip.create(Component.literal(languageModel.refreshPricesTooltip())));

        close = new IconButtonWidget(5, 20, 15, 15,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/close.png"),
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/close-highlighted.png"),
                btn -> {
                    btn.visible = false;
                    list.visible = true;
                    quantityField.visible = false;
                    recipeWidget.setX(5);
                    recipeWidget.setWidth(width - 10);
                });

        close.visible = false;

        list = new IconButtonWidget(5, 20, 15, 15,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/list.png"),
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/list-highlighted.png"),
                btn -> {
                    btn.visible = false;
                    close.visible = true;
                    quantityField.visible = true;
                    recipeWidget.setX(width / 4 + 10);
                    recipeWidget.setWidth(width - (width / 4 + 15));
                });

        list.visible = false;

        quantityField = new EditBoxWithSuggestions.Builder<>()
                .normalTexture(normalSearchTexture)
                .focusedTexture(focusedSearchTexture)
                .position(35, 167)
                .size(width / 4 - 34, 11)
                .build(font, null);

        quantityField.visible = false;
        quantityField.setResponder(string -> {
            try {
                int quantity = Integer.parseInt(string);
                if (quantity < 0) return;
                tree = recipeService.getRecipeTree(search.getSelectedSuggestion(), quantity);
            } catch (NumberFormatException ignored) {
            }
        });
        quantityField.setMaxLength(6);

        recipeWidget = ScalableWidget.builder()
                .position(width / 4 + 10, 45)
                .content(190, 40, (content, context, mouseX, mouseY, deltaTicks) -> {
                    if (tree == null) return;

                    final int nodeWidth = content.getWidth();
                    final int nodeHeight = content.getHeight();

                    final Map<Node<Shard, FusionData>, Pair<Integer, Integer>> xYPos = new HashMap<>();
                    xYPos.put(tree.root, Pair.of(content.getX(), content.getY()));

                    List<Node<Shard, FusionData>> siblings = List.of(tree.root);

                    // for blinding
                    final int middleX = content.getX() + content.getWidth() / 2;
                    int maxLeftX = Integer.MIN_VALUE;
                    int maxRightX = Integer.MAX_VALUE;

                    // position initializing
                    do {
                        List<Node<Shard, FusionData>> newSiblings = new ArrayList<>();

                        final int y = xYPos.get(siblings.getFirst()).getRight();

                        for (Node<Shard, FusionData> sibling : siblings) {
                            final int x = xYPos.get(sibling).getLeft();

                            if (!sibling.children.isEmpty()) {
                                newSiblings.addAll(sibling.children);

                                final int depth = tree.getDepth(sibling);
                                final int multiplier = (int) Math.pow(2, depth - 1); // 2^depth - 1
                                final int offset = (nodeWidth / 2 + 3) * multiplier;

                                int c = 1;

                                for (var child : sibling.children) {
                                    final int childX = x - offset * c;
                                    final int childY = y + nodeHeight + 20;

                                    c *= -1;

                                    if (childX + nodeWidth < middleX && childX + nodeWidth > maxLeftX)
                                        maxLeftX = childX + nodeWidth;
                                    else if (childX > middleX && childX < maxRightX) maxRightX = childX;

                                    xYPos.put(child, Pair.of(childX, childY));
                                }
                            }
                        }
                        siblings = newSiblings;
                    } while (!siblings.isEmpty());

                    // blinding
                    final int offsetLeft = middleX - 3 - maxLeftX;
                    final int offsetRight = maxRightX - middleX - 3;
                    for (Map.Entry<Node<Shard, FusionData>, Pair<Integer, Integer>> entry : xYPos.entrySet()) {
                        final var node = entry.getKey();
                        final var xy = entry.getValue();
                        final int nodeX = xy.getLeft();
                        final int nodeY = xy.getRight();

                        if (node.equals(tree.root)) continue;

                        if (nodeX < maxLeftX) xYPos.put(node, Pair.of(nodeX + offsetLeft, nodeY));
                        else if (nodeX >= maxRightX) xYPos.put(node, Pair.of(nodeX - offsetRight, nodeY));
                    }

                    // rendering
                    for (Map.Entry<Node<Shard, FusionData>, Pair<Integer, Integer>> entry : xYPos.entrySet()) {
                        final Node<Shard, FusionData> node = entry.getKey();
                        final Pair<Integer, Integer> xy = entry.getValue();
                        final Shard shard = node.key;
                        final FusionData fusionData = node.value;
                        final int x = xy.getLeft();
                        final int y = xy.getRight();
                        final boolean hasChildren = !node.children.isEmpty();

                        final int textureSize = nodeHeight - 10;
                        final int startY = y + 5;
                        final int startX = x + 5;
                        final int textX = startX + textureSize + 5;

                        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/" + shard.rarity + ".png"), x, y, 0f, 0f, nodeWidth, nodeHeight, nodeWidth, nodeHeight);
                        context.blit(RenderPipelines.GUI_TEXTURED, shard.texture, startX, startY, 0f, 0f, textureSize, textureSize, textureSize, textureSize);
                        context.text(font, Component.literal(shard.name).withColor(getColorByRarity(shard.rarity)).append(Component.literal(" x" + fusionData.quantity).withColor(0xFF808080)), textX, startY, 0xFFFFFFFF, false);

                        if (hasChildren) {
                            context.text(font, Component.literal(languageModel.fusions() + ": " + fusionData.fusionQuantity), textX, startY + 11, 0xFFFFFFFF, false);
                            context.text(font, Component.literal(languageModel.totalReptiles() + ": " + fusionData.pureReptiles), textX, startY + 22, 0xFFFFFFFF, false);

                            final var left = node.children.getFirst();
                            final var right = node.children.getLast();

                            final int nodeCenterX = x + nodeWidth / 2;
                            final int nodeCenterY = y + nodeHeight;
                            final int lineCenterY = y + nodeHeight + 10;
                            final int leftCenterX = xYPos.get(left).getLeft() + nodeWidth / 2;
                            final int leftCenterY = xYPos.get(left).getRight();
                            final int rightCenterX = xYPos.get(right).getLeft() + nodeWidth / 2;
                            final int rightCenterY = xYPos.get(right).getRight();

                            final Component fusionCost = Component.literal(languageModel.fusionCost() + ": ").append(getPrettyCoins(recipeService.getCheapestRecipePrice(shard))).withColor(0xFF808080);

                            context.text(font, fusionCost, nodeCenterX - font.width(fusionCost) - 3, lineCenterY - 10, 0xFFFFFFFF, false);
                            context.text(font, Component.literal(languageModel.buyCost() + ": ").append(getPrettyCoins(bazaarService.getPrice(shard, 1))).withColor(0xFF808080), nodeCenterX + 3, lineCenterY - 10, 0xFFFFFFFF, false);

                            context.fill(nodeCenterX - 1, nodeCenterY, nodeCenterX + 1, lineCenterY, getColorByRarity(shard.rarity));

                            // center
                            context.fill(leftCenterX, lineCenterY - 1, nodeCenterX, lineCenterY + 1, getColorByRarity(left.key.rarity));
                            context.fill(nodeCenterX, lineCenterY - 1, rightCenterX, lineCenterY + 1, getColorByRarity(right.key.rarity));

                            context.fill(leftCenterX - 1, lineCenterY, leftCenterX + 1, leftCenterY, getColorByRarity(left.key.rarity));
                            context.fill(rightCenterX - 1, lineCenterY, rightCenterX + 1, rightCenterY, getColorByRarity(right.key.rarity));
                        } else {
                            context.text(font, Component.literal(languageModel.quantity() + ": " + fusionData.quantity), textX, startY + 11, 0xFFFFFFFF, false);
                            context.text(font, Component.literal(languageModel.buyCost() + ": ").append(getPrettyCoins(fusionData.price)), textX, startY + 22, 0xFFFFFFFF, false);
                        }
                    }
                })
                .size(width - (width / 4 + 15), height - 50)
                .background(Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/recipe-background.png"))
                .zoomIn(Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/zoom-in.png"), Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/zoom-in-highlighted.png"))
                .zoomOut(Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/zoom-out.png"), Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/zoom-out-highlighted.png"))
                .build();

        recipeWidget.visible = false;

        minimize = new IconButtonWidget(recipeWidget.getX() + recipeWidget.getWidth() - 30, recipeWidget.getY() + recipeWidget.getHeight() - 90, 20, 20,
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/minimize.png"),
                Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/minimize-highlighted.png"),
                btn -> {
                    minimized = true;
                    savedRecipeScreen = this;
                    this.onClose();
                }
        );
        minimize.setTooltip(Tooltip.create(Component.literal(languageModel.minimize())));
        minimize.visible = false;

        addRenderableWidget(minimize);
        addRenderableWidget(recipeWidget);
        addRenderableWidget(search);
        addRenderableWidget(settings);
        addRenderableWidget(update);
        addRenderableWidget(list);
        addRenderableWidget(close);
        addRenderableWidget(quantityField);
    }

    @Override
    protected void repositionElements() {
        if (savedRecipeScreen == null) {
            super.repositionElements();
        } else {
            savedRecipeScreen = null;
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        if (search.mouseClicked(click, doubled) && !search.isMouseOverSearchField(click.x(), click.y())) {
            list.onPress(null);
            recipeWidget.visible = true;
            minimize.visible = true;
            quantityField.setValue("" + getQuantityByRarity(search.getSelectedSuggestion().rarity));

            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/background-screen.png"), 0, 0, 0f, 0f, width, height, width, height);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        extractBlurredBackground(context);

        // header
        final Identifier header = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/header.png");
        context.blit(RenderPipelines.GUI_TEXTURED, header, 0, 0, 0f, 0f, width, 40, width, 40);

        // overview
        if (close.visible) {
            // stats
            final AtomicLong totalCoins = new AtomicLong(0L);
            final AtomicInteger totalFusions = new AtomicInteger(0);
            final AtomicInteger totalReptiles = new AtomicInteger(0);
            final Map<Shard, Integer> overview = new HashMap<>();

            // recipe tree stats initialization
            if (tree != null) {
                tree.forEachTree(node -> {
                    if (node.children.isEmpty()) {
                        // total coins are equal to the sum of all leafs
                        totalCoins.addAndGet(node.value.price);

                        int quantity = node.value.quantity;
                        // get prev quantity or 0 and add it to the new quantity of shard
                        quantity += overview.getOrDefault(node.key, 0);
                        overview.put(node.key, quantity);
                    } else {
                        // calculate total fusions and reptiles for every node
                        totalFusions.addAndGet(node.value.fusionQuantity);
                        totalReptiles.addAndGet(node.value.pureReptiles);
                    }
                });
            }

            final Identifier rectangle = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/rectangle.png");

            context.blit(RenderPipelines.GUI_TEXTURED, rectangle, 5, 45, 0f, 0f, width / 4, 30, width / 4, 30);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/target.png"), 10, 50, 0f, 0f, 20, 20, 20, 20);
            context.text(font, Component.literal(languageModel.overview()).withStyle(style -> style.withBold(true)), 35, 50, 0xFFFFFFFF, false);

            int offset = 35;
            for (Map.Entry<Shard, Integer> entry : overview.entrySet()) {
                final Shard shard = entry.getKey();
                final int quantity = entry.getValue();
                final Component quantityString = Component.literal(String.valueOf(quantity));

                context.blit(RenderPipelines.GUI_TEXTURED, shard.texture, offset, 58, 0f, 0f, 7, 7, 7, 7);
                context.text(font, quantityString, offset, 67, 0xFFFFFFFF, false);

                offset += font.width(quantityString) + 5;
            }

            context.blit(RenderPipelines.GUI_TEXTURED, rectangle, 5, 80, 0f, 0f, width / 4, 30, width / 4, 30);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/dollar.png"), 10, 85, 0f, 0f, 20, 20, 20, 20);
            context.text(font, Component.literal(languageModel.totalCoins()).withStyle(style -> style.withBold(true)), 35, 85, 0xFFFFFFFF, false);
            context.text(font, getPrettyCoins(totalCoins.get()), 35, 97, 0xFFFFFFFF, false);

            context.blit(RenderPipelines.GUI_TEXTURED, rectangle, 5, 115, 0f, 0f, width / 4, 30, width / 4, 30);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/stonks.png"), 10, 120, 0f, 0f, 20, 20, 20, 20);
            context.text(font, Component.literal(languageModel.coinsSaved()).withStyle(style -> style.withBold(true)), 35, 120, 0xFFFFFFFF, false);
            context.text(font, getPrettyCoins(bazaarService.getPrice(tree.root.key, tree.root.value.quantity) - Long.parseLong(totalCoins.toString())), 35, 132, 0xFFFFFFFF, false);

            context.blit(RenderPipelines.GUI_TEXTURED, rectangle, 5, 150, 0f, 0f, width / 4, 30, width / 4, 30);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/prismarine-shard.png"), 10, 155, 0f, 0f, 20, 20, 20, 20);
            context.text(font, Component.literal(languageModel.totalShards()).withStyle(style -> style.withBold(true)), 35, 155, 0xFFFFFFFF, false);

            context.blit(RenderPipelines.GUI_TEXTURED, rectangle, 5, 185, 0f, 0f, width / 4, 30, width / 4, 30);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/loop.png"), 10, 190, 0f, 0f, 20, 20, 20, 20);
            context.text(font, Component.literal(languageModel.totalFusions()).withStyle(style -> style.withBold(true)), 35, 190, 0xFFFFFFFF, false);
            context.text(font, Component.literal(totalFusions.toString()), 35, 202, 0xFFFFFFFF, false);

            context.blit(RenderPipelines.GUI_TEXTURED, rectangle, 5, 220, 0f, 0f, width / 4, 30, width / 4, 30);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/reptile.png"), 10, 225, 0f, 0f, 20, 20, 20, 20);
            context.text(font, Component.literal(languageModel.totalReptiles()).withStyle(style -> style.withBold(true)), 35, 225, 0xFFFFFFFF, false);
            context.text(font, Component.literal(totalReptiles.toString()), 35, 237, 0xFFFFFFFF, false);
        }

        for (GuiEventListener element : children()) {
            ((Renderable) element).extractRenderState(context, mouseX, mouseY, deltaTicks);
        }
        minimize.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }

    @Contract(pure = true)
    private int getColorByRarity(@NotNull String rarity) {
        return switch (rarity) {
            case "uncommon" -> 0xFF05DF72;
            case "rare" -> 0xFF51A2FF;
            case "epic" -> 0xFFC27AFF;
            case "legendary" -> 0xFFFFD700;
            default -> 0xFFFFFFFF;
        };
    }

    @Contract(pure = true)
    private int getQuantityByRarity(@NotNull String rarity) {
        return switch (rarity) {
            case "common" -> 96;
            case "uncommon" -> 64;
            case "rare" -> 48;
            case "epic" -> 32;
            case "legendary" -> 24;
            default -> 0;
        };
    }

    private @NotNull Component getPrettyCoins(long coins) {
        String coinsString = String.valueOf(coins);
        String minus = "";
        if (coinsString.startsWith("-")) {
            minus = "-";
            coinsString = coinsString.substring(1);
        }
        if (coinsString.length() <= 3) return Component.literal(minus + coinsString);

        final float one = Float.parseFloat(coinsString.charAt(0) + "." + coinsString.substring(1));
        final float two = Float.parseFloat(coinsString.substring(0, 2) + "." + coinsString.substring(2));
        final float three = Float.parseFloat(coinsString.substring(0, 3) + "." + coinsString.substring(3));

        return switch (coinsString.length()) {
            case 4 -> Component.literal(minus + String.format("%.2f", one) + "K");
            case 5 -> Component.literal(minus + String.format("%.2f", two) + "K");
            case 6 -> Component.literal(minus + String.format("%.2f", three) + "K");
            case 7 -> Component.literal(minus + String.format("%.2f", one) + "M");
            case 8 -> Component.literal(minus + String.format("%.2f", two) + "M");
            case 9 -> Component.literal(minus + String.format("%.2f", three) + "M");
            case 10 -> Component.literal(minus + String.format("%.2f", one) + "B");
            case 11 -> Component.literal(minus + String.format("%.2f", two) + "B");
            case 12 -> Component.literal(minus + String.format("%.2f", three) + "B");
            default ->
                    Component.literal(minus + coinsString.substring(0, 3) + "Be" + (coinsString.substring(3).length() - 9));
        };
    }
}
