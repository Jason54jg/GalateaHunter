package ru.p4ejlov0d.galateahunter.service;

import org.jetbrains.annotations.NotNull;
import ru.p4ejlov0d.galateahunter.repo.Repository;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractService<R extends Repository<K, V>, K, V> {
    protected final R repo;

    protected AbstractService(@NotNull R repo) {
        this.repo = repo;
    }

    public V get(K key) {
        return repo.get(key);
    }

    public abstract CompletableFuture<?> load();

    public R getRepo() {
        return repo;
    }
}
