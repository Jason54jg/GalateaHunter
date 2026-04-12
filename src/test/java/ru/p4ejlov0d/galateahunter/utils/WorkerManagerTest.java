package ru.p4ejlov0d.galateahunter.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ALL")
class WorkerManagerTest {
    @Test
    void scheduleRepeatingTask() {
        final AtomicInteger test = new AtomicInteger();
        WorkerManager.scheduleRepeatingTask(test::incrementAndGet, 0, 2);

        try {
            Thread.sleep(2500);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(2, test.get());
    }

    @Test
    void scheduleTask() {
        final AtomicInteger test = new AtomicInteger();
        WorkerManager.scheduleTask(test::incrementAndGet, 2);
        try {
            Thread.sleep(2500);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, test.get());
    }
}