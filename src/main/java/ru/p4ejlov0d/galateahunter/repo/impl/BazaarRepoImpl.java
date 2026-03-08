package ru.p4ejlov0d.galateahunter.repo.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import ru.p4ejlov0d.galateahunter.model.Shard;
import ru.p4ejlov0d.galateahunter.model.ShardData;
import ru.p4ejlov0d.galateahunter.repo.BazaarRepo;
import ru.p4ejlov0d.galateahunter.service.ShardService;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;

public class BazaarRepoImpl implements BazaarRepo {
    private final URI bazaarEndpoint = URI.create("https://api.hypixel.net/v2/skyblock/bazaar");

    private final StampedLock lock = new StampedLock();

    private final Map<Shard, ShardData> shardPrices = new HashMap<>();

    @Override
    public void updateShardPrices() {
        final long stamp;
        if ((stamp = lock.tryWriteLock()) != 0L) {
            try (HttpClient client = HttpClient.newHttpClient()) {
                LOGGER.info("Downloading prices from {}", bazaarEndpoint);

                final HttpRequest request = HttpRequest.newBuilder(bazaarEndpoint).timeout(Duration.ofSeconds(15)).GET().build();
                final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                final JsonElement element = JsonParser.parseString(response.body());
                final String prettyJson = gson.toJson(element);

                try (BufferedReader reader = new BufferedReader(new StringReader(prettyJson))) {
                    String line;
                    shardPrices.clear();

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();

                        if (line.startsWith("\"SHARD_")) {
                            Shard shard = null;

                            for (Shard s : ShardService.INSTANCE.getShards()) {
                                if (s.internalId.equals(line.split("\"")[1])) {
                                    shard = s;
                                    break;
                                }
                            }

                            final ShardData shardData = new ShardData(new ArrayList<>(), new ArrayList<>());

                            while (!(line = reader.readLine()).contains("\"quick_status\": {")) {
                                line = line.trim();

                                if (line.equals("\"sell_summary\": [") || line.equals("\"buy_summary\": [")) {
                                    final String summary = line;

                                    while (!reader.readLine().contains("],")) {
                                        int amount = 0;
                                        double pricePerUnit = 0d;

                                        while (!(line = reader.readLine()).contains("}")) {
                                            line = line.trim();

                                            if (line.startsWith("\"amount\": "))
                                                amount = Integer.parseInt(line.substring("\"amount\": ".length()).split(",")[0]);
                                            if (line.startsWith("\"pricePerUnit\": "))
                                                pricePerUnit = Double.parseDouble(line.substring("\"pricePerUnit\": ".length()).split(",")[0]);
                                        }

                                        switch (summary) {
                                            case "\"sell_summary\": [" ->
                                                    shardData.sellSummary().add(new ShardData.Summary(amount, pricePerUnit));
                                            case "\"buy_summary\": [" ->
                                                    shardData.buySummary().add(new ShardData.Summary(amount, pricePerUnit));
                                        }
                                    }
                                }
                            }
                            shardPrices.put(shard, shardData);
                        }
                    }
                    LOGGER.info("Successfully downloaded prices from {}", bazaarEndpoint);
                }
            } catch (Exception e) {
                LOGGER.warn("An error occurred while trying to download shard prices from {}", bazaarEndpoint, e);
                shardPrices.clear();
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public @Nullable ShardData get(Shard shard) {
        // avoid concurrent modification exception and intermediate results
        if (lock.isWriteLocked()) return null;

        return shardPrices.get(shard);
    }
}
