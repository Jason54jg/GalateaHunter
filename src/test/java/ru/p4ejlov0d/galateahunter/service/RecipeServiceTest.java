package ru.p4ejlov0d.galateahunter.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.model.FusionData;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.utils.WorkerManager;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
class RecipeServiceTest {
    private final RecipeService service = RecipeService.getInstance();

    @BeforeAll
    static void beforeAll() {
        TestUtils.loadShards();
    }

    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void load() {
        while (WorkerManager.singleThreadPool.getActiveCount() != 0) {
        }

        service.load().thenRun(() -> {
            try {
                final Field cheapestRecipes = service.getClass().getDeclaredField("cheapestRecipes");
                cheapestRecipes.setAccessible(true);

                final var map = ((Map<Shard, FusionData>) cheapestRecipes.get(service));

                assertTrue(map.isEmpty() || map.size() >= 181);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Test
    void getRecipeTree() {
        final Shard chameleon = ShardService.INSTANCE.get("l4");
        final var tree = service.getRecipeTree(chameleon, 24);

        assertEquals(tree.root.key, chameleon);
        assertEquals(tree.root.value.quantity, 24);

        final AtomicInteger nodeCounter = new AtomicInteger(0);
        tree.forEachTree(node -> {
            nodeCounter.incrementAndGet();
        });
        assertEquals(nodeCounter.get(), 1);
    }

    @Test
    void getCheapestRecipePrice() {
        final Shard chameleon = ShardService.INSTANCE.get("l4");
        final long price = BazaarService.INSTANCE.getPrice(chameleon, 1);

        assertTrue(service.getCheapestRecipePrice(chameleon) == price);
    }
}