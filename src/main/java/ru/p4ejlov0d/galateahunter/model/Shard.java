package ru.p4ejlov0d.galateahunter.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class Shard {
    @Expose(deserialize = false)
    public Identifier texture;

    @Expose(deserialize = false)
    public String id;

    public String name;
    public String family;
    public String type;
    public String rarity;

    @SerializedName("fuse_amount")
    public int fuseAmount;

    @SerializedName("internal_id")
    public String internalId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Shard shard)) return false;
        return fuseAmount == shard.fuseAmount && Objects.equals(texture, shard.texture) && Objects.equals(id, shard.id) && Objects.equals(name, shard.name) && Objects.equals(family, shard.family) && Objects.equals(type, shard.type) && Objects.equals(rarity, shard.rarity) && Objects.equals(internalId, shard.internalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, id, name, family, type, rarity, fuseAmount, internalId);
    }
}
