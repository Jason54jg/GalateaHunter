package ru.p4ejlov0d.galateahunter.utils.tree;

import java.util.ArrayList;
import java.util.List;

public class Node<K, V> {
    public final V value;
    public final K key;
    public final List<Node<K, V>> children = new ArrayList<>();

    Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
