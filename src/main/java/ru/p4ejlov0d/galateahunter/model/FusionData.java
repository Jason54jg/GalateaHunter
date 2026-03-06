package ru.p4ejlov0d.galateahunter.model;

import org.jetbrains.annotations.NotNull;

public class FusionData implements Comparable<FusionData> {
    public Shard shard;
    public long price;
    public int fusionQuantity;
    public int pureReptiles;
    public int quantity;
    public Shard left;
    public Shard right;

    @Override
    public int compareTo(@NotNull FusionData o) {
        return Long.compare(price, o.price);
    }
}
