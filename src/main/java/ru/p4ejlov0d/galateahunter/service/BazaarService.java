package ru.p4ejlov0d.galateahunter.service;

import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.model.ShardData;
import ru.p4ejlov0d.galateahunter.repo.BazaarRepo;
import ru.p4ejlov0d.galateahunter.repo.impl.BazaarRepoImpl;
import ru.p4ejlov0d.galateahunter.utils.WorkerManager;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BazaarService extends AbstractService<BazaarRepo, Shard, ShardData> {
    public static final BazaarService INSTANCE = new BazaarService();

    private final Map<Shard, ShardData> mutableShardPrices = new HashMap<>();

    public volatile boolean isUpdated = false;

    private BazaarService() {
        super(new BazaarRepoImpl());

        WorkerManager.scheduleTask(() -> {
            repo.updateShardPrices();
            isUpdated = true;
        }, 0, 600);
    }

    @Override
    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(repo::updateShardPrices, WorkerManager.singleThreadPool).thenRun(() -> isUpdated = true);
    }

    public long getPrice(final Shard shard, int quantity) {
        final ShardData shardData;

        if ((shardData = get(shard)) != null) {
            long price = 0L;
            switch (ModConfigHolder.getConfig().recipe.bazaarStrategy) {
                case BUY_OFFER -> {
                    final List<ShardData.Summary> summary = shardData.sellSummary();
                    final List<ShardData.Summary> buySummary = shardData.buySummary();

                    //if summary exists, price = best offer * quantity
                    if (!summary.isEmpty()) price = (long) (summary.getFirst().pricePerUnit() * quantity);
                        // assume price in the best instant buy offer - 30%
                    else if (!buySummary.isEmpty())
                        price = (long) (buySummary.getFirst().pricePerUnit() - buySummary.getFirst().pricePerUnit() * 30 / 100);
                        // else idk
                    else price = Long.MAX_VALUE / 2;
                }
                case INSTANT_BUY -> {
                    final List<ShardData.Summary> summary = shardData.buySummary();

                    for (ShardData.Summary s : summary) {
                        // when your order was filled (or it 0 on start)
                        if (quantity == 0) break;

                        // adds left quantity * order price to price
                        if (quantity < s.amount()) {
                            price += (long) (s.pricePerUnit() * quantity);
                            quantity = 0;
                            break;
                        }

                        // eats order and erases quantity - order amount
                        price += (long) (s.pricePerUnit() * s.amount());
                        quantity -= s.amount();
                    }

                    // happens when orders amount < quantity (you can't fill your order, you can only craft it) or summary is empty
                    if (summary.isEmpty() || quantity != 0) price = Long.MAX_VALUE / 2;
                }
            }

            return price;
        }
        // returns if prices has not been loaded
        return Long.MAX_VALUE / 2;
    }

    public void purchase(final Shard shard, int quantity) {
        final ShardData shardData;

        if ((shardData = get(shard)) != null) {
            final List<ShardData.Summary> summary = shardData.buySummary();

            for (ShardData.Summary s : summary) {
                if (quantity < s.amount()) {
                    shardData.purchaseTo(quantity, s);
                    return;
                }
                quantity -= s.amount();
            }
            if (!summary.isEmpty()) shardData.purchaseTo(summary.getLast().amount(), summary.getLast());
        }
    }

    @Override
    public @Nullable ShardData get(final Shard key) {
        ShardData shardData = repo.get(key);

        if (shardData == null) return null;

        if (!mutableShardPrices.containsKey(key)) {
            shardData = shardData.copy();
            mutableShardPrices.put(key, shardData);
        } else return mutableShardPrices.get(key);

        return shardData;
    }

    public void restoreShardPrices() {
        mutableShardPrices.clear();
    }
}
