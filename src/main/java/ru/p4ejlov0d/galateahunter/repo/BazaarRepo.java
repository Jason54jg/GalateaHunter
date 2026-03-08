package ru.p4ejlov0d.galateahunter.repo;

import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.model.ShardData;

public interface BazaarRepo extends Repository<Shard, ShardData> {
    void updateShardPrices();
}
