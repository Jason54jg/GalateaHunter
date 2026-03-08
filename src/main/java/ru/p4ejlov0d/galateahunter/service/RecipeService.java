package ru.p4ejlov0d.galateahunter.service;

import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.config.GalateaHunterConfig;
import ru.p4ejlov0d.galateahunter.model.FusionData;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.repo.RecipeRepo;
import ru.p4ejlov0d.galateahunter.repo.impl.RecipeRepoImpl;
import ru.p4ejlov0d.galateahunter.utils.WorkerManager;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;
import ru.p4ejlov0d.galateahunter.utils.tree.Node;
import ru.p4ejlov0d.galateahunter.utils.tree.Tree;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;

public class RecipeService extends AbstractService<RecipeRepo, Shard, List<FusionData>> {
    private static RecipeService instance;

    private final BazaarService bazaarService = BazaarService.INSTANCE;

    private final Map<Shard, FusionData> cheapestRecipes = new HashMap<>();

    private Tree<Shard, FusionData> tree;
    private int oldQuantity;
    private CompletableFuture<Void> updateFuture;

    private RecipeService() {
        super(new RecipeRepoImpl());
        load();
    }

    public static RecipeService getInstance() {
        return instance == null ? instance = new RecipeService() : instance;
    }

    @Override
    public CompletableFuture<Void> load() {
        return updateFuture = CompletableFuture.runAsync(this::updateCheapestRecipes, WorkerManager.singleThreadPool).thenRun(() -> bazaarService.isUpdated = false);
    }

    public @NotNull Tree<Shard, FusionData> getRecipeTree(final Shard selected, int quantity) {
        if (tree != null && tree.root.key.equals(selected) && oldQuantity == quantity) return tree;

        bazaarService.restoreShardPrices();

        oldQuantity = quantity;

        final FusionData root = new FusionData();

        root.shard = selected;
        root.quantity = quantity;

        if (cheapestRecipes.isEmpty() || bazaarService.isUpdated) {
            if (updateFuture.isDone()) load();

            updateFuture.join();
        }

        root.price = bazaarService.getPrice(selected, quantity);

        tree = new Tree<>(selected, root);

        parseNode(selected, tree.root, List.of(selected), quantity);

        return tree;
    }

    private void parseNode(Shard shard, Node<Shard, FusionData> parent, List<Shard> visited, int quantity) {
        FusionData f = cheapestRecipes.get(shard);
        Shard left = f.left;
        Shard right = f.right;

        // leafs
        if (left == null || right == null || visited.contains(left) || visited.contains(right)) {
            if (parent.equals(tree.root)) return;

            bazaarService.purchase(shard, quantity);
            return;
        }

        quantity = (int) Math.ceil((double) quantity / f.quantity) * f.quantity;

        int fusionQuantity = quantity / f.quantity;
        int pureReptiles = 0;

        if (left.family.contains("Reptile") || right.family.contains("Reptile"))
            fusionQuantity -= pureReptiles += (int) (fusionQuantity * pureReptileChance() / 100f);

        final int requiredQuantityLeft = fusionQuantity * left.fuseAmount;
        final int requiredQuantityRight = fusionQuantity * right.fuseAmount;

        var currentNode = tree.search(parent, shard);

        // 2 same children
        if (!currentNode.children.isEmpty()) currentNode = parent.children.getLast();

        final long leftNodePrice = bazaarService.getPrice(left, requiredQuantityLeft);
        final long rightNodePrice = bazaarService.getPrice(right, requiredQuantityRight);

        final int testRootQuantity = (int) Math.ceil((double) tree.root.value.quantity / cheapestRecipes.get(tree.root.key).quantity) * cheapestRecipes.get(tree.root.key).quantity;

        if (Math.min(leftNodePrice, cheapestRecipes.get(left).price * requiredQuantityLeft) + Math.min(leftNodePrice, cheapestRecipes.get(right).price * requiredQuantityRight) > bazaarService.getPrice(tree.root.key, testRootQuantity)) {
            bazaarService.purchase(shard, quantity);
            return;
        }

        currentNode.value.quantity = quantity;
        currentNode.value.price = bazaarService.getPrice(shard, quantity);
        currentNode.value.fusionQuantity = fusionQuantity;
        currentNode.value.pureReptiles = pureReptiles;

        FusionData leftData = new FusionData();
        FusionData rightData = new FusionData();
        List<Shard> visitedLeft = new ArrayList<>(visited);
        List<Shard> visitedRight = new ArrayList<>(visited);

        visitedLeft.add(left);
        visitedRight.add(right);

        leftData.shard = left;
        rightData.shard = right;
        leftData.quantity = requiredQuantityLeft;
        rightData.quantity = requiredQuantityRight;
        leftData.price = leftNodePrice;
        rightData.price = rightNodePrice;

        tree.insert(currentNode, left, leftData);
        tree.insert(currentNode, right, rightData);

        parseNode(left, currentNode, visitedLeft, requiredQuantityLeft);
        parseNode(right, currentNode, visitedRight, requiredQuantityRight);
    }

    private void updateCheapestRecipes() {
        final Map<Shard, List<FusionData>> shardRecipes = repo.getShardRecipes();

        if (shardRecipes.isEmpty()) return;

        LOGGER.info("Updating cheapest recipes...");

        cheapestRecipes.clear();

        // init result, find leafs
        for (var entry : shardRecipes.entrySet()) {
            Shard shard = entry.getKey();
            long price = bazaarService.getPrice(shard, 1);
            FusionData f = new FusionData();

            f.price = price;
            f.quantity = 1;
            f.shard = shard;

            cheapestRecipes.put(shard, f);

            for (var fusionData : entry.getValue()) {
                int quantity = fusionData.quantity;

                Shard left = fusionData.left;
                Shard right = fusionData.right;

                // price per shard
                long pairPrice = (bazaarService.getPrice(left, left.fuseAmount) + bazaarService.getPrice(right, right.fuseAmount)) / quantity;

                if (price > pairPrice) {
                    price = pairPrice;
                    f.price = price;
                    f.quantity = quantity;
                    f.left = left;
                    f.right = right;
                }
            }
        }

        Set<Shard> leafs = new HashSet<>();
        // do not exclude leafs from queue
        Queue<FusionData> queue = new LinkedList<>(ShardService.INSTANCE.getShards().stream().filter(s -> {
            // exclude shards like chameleon
            if (!shardRecipes.get(s).isEmpty()) return true;
            leafs.add(s);
            return false;
        }).map(cheapestRecipes::get).sorted().toList());

        // add min units
        cheapestRecipes.values().stream().filter(f -> f.left != null && f.right != null).forEach(f -> {
            leafs.add(f.left);
            leafs.add(f.right);
        });

        int counter = 0;

        while (!queue.isEmpty()) {
            FusionData f = queue.poll();
            long price = bazaarService.getPrice(f.shard, 1);
            boolean isUpdated = false;

            for (FusionData fusionData : shardRecipes.get(f.shard)) {
                int quantity = fusionData.quantity;

                Shard left = fusionData.left;
                Shard right = fusionData.right;

                if (leafs.contains(left) && leafs.contains(right)) {
                    long sumPrice = (cheapestRecipes.get(left).price * left.fuseAmount + cheapestRecipes.get(right).price * right.fuseAmount) / quantity;

                    if (left.family.contains("Reptile") || right.family.contains("Reptile")) {
                        float reptile = pureReptileChance() / 100;
                        float requiredQuantityLeft = left.fuseAmount - reptile * left.fuseAmount;
                        float requiredQuantityRight = right.fuseAmount - reptile * right.fuseAmount;
                        sumPrice = (long) ((cheapestRecipes.get(left).price * requiredQuantityLeft + cheapestRecipes.get(right).price * requiredQuantityRight) / quantity);
                    }

                    if (price > sumPrice) {
                        price = sumPrice;
                        leafs.add(f.shard);
                        f.price = price;
                        f.quantity = quantity;
                        f.left = left;
                        f.right = right;
                        isUpdated = true;
                    }
                }
            }

            if (counter == queue.size()) {
                if (!queue.isEmpty()) {
                    leafs.add(queue.stream().sorted().findFirst().orElse(queue.peek()).shard);
                    counter = 0;
                    queue.add(f);
                    continue;
                }
                leafs.add(f.shard);
                break;
            }

            if (!isUpdated && !leafs.contains(f.shard)) {
                queue.add(f);
                counter++;
            } else {
                counter = 0;
            }
        }
    }

    public long getCheapestRecipePrice(Shard shard) {
        return cheapestRecipes.get(shard).price;
    }

    /**
     *
     * @return A chance number (26.0 for example)
     */
    private float pureReptileChance() {
        final GalateaHunterConfig.Recipe recipeConfig = ModConfigHolder.getConfig().recipe;

        final int crocodileLevel = recipeConfig.crocodileLevel;
        final int seaSerpentLevel = recipeConfig.seaSerpentLevel;
        final int tiamatLevel = recipeConfig.tiamatLevel;

        /*
            Pure Reptile Chance = Crocodile Level * 2 + (Sea Serpent Level * 2 + (Tiamat level * 5)%)%
            Max stats: 20 + (20 + 50%)% = 20 + 30% = 26% chance for pure reptile trigger per fusion (1 trigger in about 3.8 fusions)
        */
        return crocodileLevel * 2f + crocodileLevel * 2f * (seaSerpentLevel * 2f + seaSerpentLevel * 2f * tiamatLevel * 5f / 100f) / 100f;
    }
}
