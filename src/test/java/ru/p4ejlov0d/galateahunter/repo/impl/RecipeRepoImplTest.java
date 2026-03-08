package ru.p4ejlov0d.galateahunter.repo.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.repo.RecipeRepo;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
class RecipeRepoImplTest {
    @BeforeAll
    static void beforeAll() {
        TestUtils.loadShards();
    }

    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void getShardRecipes() {
        final RecipeRepo recipeRepo = new RecipeRepoImpl();
        final var shardRecipes = recipeRepo.getShardRecipes();

        assertTrue(shardRecipes.isEmpty() || shardRecipes.size() >= 181);
        assertTrue(shardRecipes.get(ShardService.INSTANCE.get("l4")).isEmpty());
        assertFalse(shardRecipes.get(ShardService.INSTANCE.get("c1")).isEmpty());
    }

    @Test
    void get() {
        final RecipeRepo recipeRepo = new RecipeRepoImpl();

        assertTrue(recipeRepo.get(ShardService.INSTANCE.get("l4")).isEmpty());
        assertFalse(recipeRepo.get(ShardService.INSTANCE.get("c1")).isEmpty());
    }
}