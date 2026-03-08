package ru.p4ejlov0d.galateahunter.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.FusionData;
import ru.p4ejlov0d.galateahunter.model.LanguageModel;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.screen.widget.IconButtonWidget;
import ru.p4ejlov0d.galateahunter.screen.widget.ScalableWidget;
import ru.p4ejlov0d.galateahunter.screen.widget.TextFieldWidgetWithSuggestions;
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

@Environment(EnvType.CLIENT)
public class RecipeScreen extends Screen {
    private static final Text TITLE = Text.literal("Recipe");

    private final BazaarService bazaarService = BazaarService.INSTANCE;
    private final ShardService shardService = ShardService.INSTANCE;
    private final RecipeService recipeService = RecipeService.getInstance();

    private LanguageModel languageModel;
    private String searchText;
    private Tree<Shard, FusionData> tree;

    private TextFieldWidgetWithSuggestions search;
    private IconButtonWidget close;
    private IconButtonWidget list;
    private TextFieldWidget quantityField;
    private ScalableWidget recipeWidget;

    public RecipeScreen() {
        super(TITLE);
    }

    public RecipeScreen(@Nullable String searchText) {
        this();
        this.searchText = searchText;
    }

    @Override
    protected void init() {
        languageModel = LanguageResourceHandler.getInstance().getLanguageModel();

        search = new TextFieldWidgetWithSuggestions(textRenderer, (int) (width / 3.3333333d), 5, (int) (width / 2.5d), 30, shardService.getShards());
        search.setPlaceholder(Text.literal(languageModel.search()));

        if (searchText != null) {
            search.write(searchText);
            this.setFocused(search);
        }

        final IconButtonWidget settings = new IconButtonWidget(width - 30, 10, 20, 20,
                Identifier.of(MOD_ID, "textures/gui/settings.png"),
                Identifier.of(MOD_ID, "textures/gui/settings-highlighted.png"),
                btn -> client.setScreen(GalateaHunterScreen.createGui(this, languageModel.huntingCategory()))
        );

        final IconButtonWidget update = new IconButtonWidget(width - 55, 10, 20, 20,
                Identifier.of(MOD_ID, "textures/gui/refresh.png"),
                Identifier.of(MOD_ID, "textures/gui/refresh-highlighted.png"),
                btn -> bazaarService.load()
        );

        update.setTooltip(Tooltip.of(Text.literal(languageModel.refreshPricesTooltip())));

        close = new IconButtonWidget(5, 20, 15, 15,
                Identifier.of(MOD_ID, "textures/gui/close.png"),
                Identifier.of(MOD_ID, "textures/gui/close-highlighted.png"),
                btn -> {
                    btn.visible = false;
                    list.visible = true;
                    quantityField.visible = false;
                    recipeWidget.setX(5);
                    recipeWidget.setBounds(width - 10, null);
                });

        close.visible = false;

        list = new IconButtonWidget(5, 20, 15, 15,
                Identifier.of(MOD_ID, "textures/gui/list.png"),
                Identifier.of(MOD_ID, "textures/gui/list-highlighted.png"),
                btn -> {
                    btn.visible = false;
                    close.visible = true;
                    quantityField.visible = true;
                    recipeWidget.setX(width / 4 + 10);
                    recipeWidget.setBounds(width - (width / 4 + 15), null);
                });

        list.visible = false;

        quantityField = new TextFieldWidgetWithSuggestions(textRenderer, 35, 167, width / 4 - 35, 10);
        quantityField.visible = false;
        quantityField.setChangedListener(string -> {
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

                        context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/" + shard.rarity + ".png"), x, y, 0f, 0f, nodeWidth, nodeHeight, nodeWidth, nodeHeight);
                        context.drawTexture(RenderLayer::getGuiTextured, shard.texture, startX, startY, 0f, 0f, textureSize, textureSize, textureSize, textureSize);
                        context.drawText(textRenderer, Text.literal(shard.name).withColor(getColorByRarity(shard.rarity)).append(Text.literal(" x" + fusionData.quantity).withColor(0xFF808080)), textX, startY, 0xFFFFFFFF, false);

                        if (hasChildren) {
                            context.drawText(textRenderer, Text.literal(languageModel.fusions() + ": " + fusionData.fusionQuantity), textX, startY + 11, 0xFFFFFFFF, false);
                            context.drawText(textRenderer, Text.literal(languageModel.totalReptiles() + ": " + fusionData.pureReptiles), textX, startY + 22, 0xFFFFFFFF, false);

                            final var left = node.children.getFirst();
                            final var right = node.children.getLast();

                            final int nodeCenterX = x + nodeWidth / 2;
                            final int nodeCenterY = y + nodeHeight;
                            final int lineCenterY = y + nodeHeight + 10;
                            final int leftCenterX = xYPos.get(left).getLeft() + nodeWidth / 2;
                            final int leftCenterY = xYPos.get(left).getRight();
                            final int rightCenterX = xYPos.get(right).getLeft() + nodeWidth / 2;
                            final int rightCenterY = xYPos.get(right).getRight();

                            final Text fusionCost = Text.literal(languageModel.fusionCost() + ": ").append(getPrettyCoins(recipeService.getCheapestRecipePrice(shard))).withColor(0xFF808080);

                            context.drawText(textRenderer, fusionCost, nodeCenterX - textRenderer.getWidth(fusionCost) - 3, lineCenterY - 10, 0xFFFFFFFF, false);
                            context.drawText(textRenderer, Text.literal(languageModel.buyCost() + ": ").append(getPrettyCoins(bazaarService.getPrice(shard, 1))).withColor(0xFF808080), nodeCenterX + 3, lineCenterY - 10, 0xFFFFFFFF, false);

                            context.fill(nodeCenterX - 1, nodeCenterY, nodeCenterX + 1, lineCenterY, getColorByRarity(shard.rarity));

                            // center
                            context.fill(leftCenterX, lineCenterY - 1, nodeCenterX, lineCenterY + 1, getColorByRarity(left.key.rarity));
                            context.fill(nodeCenterX, lineCenterY - 1, rightCenterX, lineCenterY + 1, getColorByRarity(right.key.rarity));

                            context.fill(leftCenterX - 1, lineCenterY, leftCenterX + 1, leftCenterY, getColorByRarity(left.key.rarity));
                            context.fill(rightCenterX - 1, lineCenterY, rightCenterX + 1, rightCenterY, getColorByRarity(right.key.rarity));
                        } else {
                            context.drawText(textRenderer, Text.literal(languageModel.quantity() + ": " + fusionData.quantity), textX, startY + 11, 0xFFFFFFFF, false);
                            context.drawText(textRenderer, Text.literal(languageModel.buyCost() + ": ").append(getPrettyCoins(fusionData.price)), textX, startY + 22, 0xFFFFFFFF, false);
                        }
                    }
                })
                .size(width - (width / 4 + 15), height - 50)
                .background(Identifier.of(MOD_ID, "textures/gui/recipe-background.png"))
                .zoomIn(Identifier.of(MOD_ID, "textures/gui/zoom-in.png"), Identifier.of(MOD_ID, "textures/gui/zoom-in-highlighted.png"))
                .zoomOut(Identifier.of(MOD_ID, "textures/gui/zoom-out.png"), Identifier.of(MOD_ID, "textures/gui/zoom-out-highlighted.png"))
                .build();

        recipeWidget.visible = false;

        addDrawableChild(recipeWidget);
        addDrawableChild(search);
        addDrawableChild(settings);
        addDrawableChild(update);
        addDrawableChild(list);
        addDrawableChild(close);
        addDrawableChild(quantityField);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (search.interviewChildren(mouseX, mouseY, button)) {
            list.onPress();
            recipeWidget.visible = true;
            quantityField.setText("" + getQuantityByRarity(search.getSelectedSuggestion().rarity));

            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/background-screen.png"), 0, 0, 0f, 0f, width, height, width, height);
        context.draw();
        applyBlur();

        // header
        final Identifier header = Identifier.of(MOD_ID, "textures/gui/header.png");
        context.drawTexture(RenderLayer::getGuiTextured, header, 0, 0, 0f, 0f, width, 40, width, 40);

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

            final Identifier rectangle = Identifier.of(MOD_ID, "textures/gui/rectangle.png");

            context.drawTexture(RenderLayer::getGuiTextured, rectangle, 5, 45, 0f, 0f, width / 4, 30, width / 4, 30);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/target.png"), 10, 50, 0f, 0f, 20, 20, 20, 20);
            context.drawText(textRenderer, Text.literal(languageModel.overview()).styled(style -> style.withBold(true)), 35, 50, 0xFFFFFFFF, false);

            int offset = 35;
            for (Map.Entry<Shard, Integer> entry : overview.entrySet()) {
                final Shard shard = entry.getKey();
                final int quantity = entry.getValue();
                final Text quantityString = Text.literal(String.valueOf(quantity));

                context.drawTexture(RenderLayer::getGuiTextured, shard.texture, offset, 58, 0f, 0f, 7, 7, 7, 7);
                context.drawText(textRenderer, quantityString, offset, 67, 0xFFFFFFFF, false);

                offset += textRenderer.getWidth(quantityString) + 5;
            }

            context.drawTexture(RenderLayer::getGuiTextured, rectangle, 5, 80, 0f, 0f, width / 4, 30, width / 4, 30);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/dollar.png"), 10, 85, 0f, 0f, 20, 20, 20, 20);
            context.drawText(textRenderer, Text.literal(languageModel.totalCoins()).styled(style -> style.withBold(true)), 35, 85, 0xFFFFFFFF, false);
            context.drawText(textRenderer, getPrettyCoins(totalCoins.get()), 35, 97, 0xFFFFFFFF, false);

            context.drawTexture(RenderLayer::getGuiTextured, rectangle, 5, 115, 0f, 0f, width / 4, 30, width / 4, 30);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/stonks.png"), 10, 120, 0f, 0f, 20, 20, 20, 20);
            context.drawText(textRenderer, Text.literal(languageModel.coinsSaved()).styled(style -> style.withBold(true)), 35, 120, 0xFFFFFFFF, false);
            context.drawText(textRenderer, getPrettyCoins(bazaarService.getPrice(tree.root.key, tree.root.value.quantity) - Long.parseLong(totalCoins.toString())), 35, 132, 0xFFFFFFFF, false);

            context.drawTexture(RenderLayer::getGuiTextured, rectangle, 5, 150, 0f, 0f, width / 4, 30, width / 4, 30);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/prismarine-shard.png"), 10, 155, 0f, 0f, 20, 20, 20, 20);
            context.drawText(textRenderer, Text.literal(languageModel.totalShards()).styled(style -> style.withBold(true)), 35, 155, 0xFFFFFFFF, false);

            context.drawTexture(RenderLayer::getGuiTextured, rectangle, 5, 185, 0f, 0f, width / 4, 30, width / 4, 30);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/loop.png"), 10, 190, 0f, 0f, 20, 20, 20, 20);
            context.drawText(textRenderer, Text.literal(languageModel.totalFusions()).styled(style -> style.withBold(true)), 35, 190, 0xFFFFFFFF, false);
            context.drawText(textRenderer, Text.literal(totalFusions.toString()), 35, 202, 0xFFFFFFFF, false);

            context.drawTexture(RenderLayer::getGuiTextured, rectangle, 5, 220, 0f, 0f, width / 4, 30, width / 4, 30);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MOD_ID, "textures/gui/reptile.png"), 10, 225, 0f, 0f, 20, 20, 20, 20);
            context.drawText(textRenderer, Text.literal(languageModel.totalReptiles()).styled(style -> style.withBold(true)), 35, 225, 0xFFFFFFFF, false);
            context.drawText(textRenderer, Text.literal(totalReptiles.toString()), 35, 237, 0xFFFFFFFF, false);
        }

        for (Element element : children()) {
            ((Drawable) element).render(context, mouseX, mouseY, deltaTicks);
        }
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

    private @NotNull Text getPrettyCoins(long coins) {
        String coinsString = String.valueOf(coins);
        String minus = "";
        if (coinsString.startsWith("-")) {
            minus = "-";
            coinsString = coinsString.substring(1);
        }
        if (coinsString.length() <= 3) return Text.literal(minus + coinsString);

        final float one = Float.parseFloat(coinsString.charAt(0) + "." + coinsString.substring(1));
        final float two = Float.parseFloat(coinsString.substring(0, 2) + "." + coinsString.substring(2));
        final float three = Float.parseFloat(coinsString.substring(0, 3) + "." + coinsString.substring(3));

        return switch (coinsString.length()) {
            case 4 -> Text.literal(minus + String.format("%.2f", one) + "K");
            case 5 -> Text.literal(minus + String.format("%.2f", two) + "K");
            case 6 -> Text.literal(minus + String.format("%.2f", three) + "K");
            case 7 -> Text.literal(minus + String.format("%.2f", one) + "M");
            case 8 -> Text.literal(minus + String.format("%.2f", two) + "M");
            case 9 -> Text.literal(minus + String.format("%.2f", three) + "M");
            case 10 -> Text.literal(minus + String.format("%.2f", one) + "B");
            case 11 -> Text.literal(minus + String.format("%.2f", two) + "B");
            case 12 -> Text.literal(minus + String.format("%.2f", three) + "B");
            default ->
                    Text.literal(minus + coinsString.substring(0, 3) + "Be" + (coinsString.substring(3).length() - 9));
        };
    }
}
