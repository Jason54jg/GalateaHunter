package ru.p4ejlov0d.galateahunter.repo;

public interface Repository<K, V> {
    V get(K key);
}
