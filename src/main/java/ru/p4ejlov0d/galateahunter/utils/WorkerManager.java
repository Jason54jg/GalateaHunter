package ru.p4ejlov0d.galateahunter.utils;

import java.util.concurrent.*;

public final class WorkerManager {
    public static final ThreadPoolExecutor singleThreadPool = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    private static final ScheduledExecutorService repeatingScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private WorkerManager() {
    }

    public static void scheduleRepeatingTask(Runnable task, long startAfterSeconds, long seconds) {
        repeatingScheduler.scheduleWithFixedDelay(task, startAfterSeconds, seconds, TimeUnit.SECONDS);
    }

    public static void scheduleTask(Runnable task, long seconds) {
        scheduler.schedule(task, seconds, TimeUnit.SECONDS);
    }
}
