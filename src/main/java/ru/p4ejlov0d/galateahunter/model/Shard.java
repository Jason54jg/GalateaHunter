package ru.p4ejlov0d.galateahunter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class Shard {
    @JsonIgnore
    public Identifier texture;

    @JsonIgnore
    public String id;

    public String name;
    public String family;
    public String type;
    public String rarity;

    @JsonProperty("fuse_amount")
    public int fuseAmount;

    @JsonProperty("internal_id")
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
