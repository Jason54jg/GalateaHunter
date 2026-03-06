package ru.p4ejlov0d.galateahunter.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import ru.p4ejlov0d.galateahunter.TestUtils;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
class RemoteRepositoryTest {
    @AfterAll
    static void afterAll() {
        TestUtils.clearConfig();
    }

    @Test
    void checkForUpdates() {
        ModConfigHolder.register();
        RemoteRepository.getInstance().cloneRepoWithImages();

        ModConfigHolder.getConfig().imagesCount = 0;
        ModConfigHolder.save();

        RemoteRepository.getInstance().checkForUpdates().thenRun(
                () -> assertTrue(RemoteRepository.getInstance().isNeedUpdate())
        ).join();
    }
}