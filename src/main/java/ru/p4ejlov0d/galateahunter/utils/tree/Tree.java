package ru.p4ejlov0d.galateahunter.utils.tree;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class Tree<K, V> {
    public final Node<K, V> root;

    public Tree(K key, V value) {
        root = new Node<>(key, value);
    }

    public void insert(Node<K, V> parent, K key, V value) {
        if (parent == null) return;

        parent.children.add(new Node<>(key, value));
    }

    public Node<K, V> search(@NotNull Node<K, V> node, K key) {
        if (node.key.equals(key)) return node;

        for (Node<K, V> child : node.children) {
            Node<K, V> n = search(child, key);

            if (n != null) return n;
        }

        return null;
    }

    public void forEachTree(@NotNull Consumer<Node<K, V>> action) {
        action.accept(root);
        forEachTree(root, action);
    }

    private void forEachTree(@NotNull Node<K, V> node, Consumer<Node<K, V>> action) {
        for (Node<K, V> child : node.children) {
            action.accept(child);
            forEachTree(child, action);
        }
    }

    public int getDepth(Node<K, V> node) {
        if (node == null) return -1;

        final List<Integer> heights = new ArrayList<>();

        for (Node<K, V> child : node.children) {
            heights.add(getDepth(child));
        }

        return heights.stream().max(Comparator.naturalOrder()).orElse(-1) + 1;
    }
}
