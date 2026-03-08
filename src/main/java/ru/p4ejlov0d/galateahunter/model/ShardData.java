package ru.p4ejlov0d.galateahunter.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record ShardData(List<Summary> sellSummary, List<Summary> buySummary) {
    @Override
    public @NotNull List<Summary> sellSummary() {
        sellSummary.sort(Comparator.comparingDouble(Summary::pricePerUnit).reversed());
        return sellSummary;
    }

    @Override
    public @NotNull List<Summary> buySummary() {
        buySummary.sort(Comparator.comparingDouble(Summary::pricePerUnit));
        return buySummary;
    }

    public void purchaseTo(int remainingAmount, @NotNull Summary summary) {
        if (!buySummary().contains(summary)) return;

        final var iterator = buySummary.listIterator();

        while (iterator.hasNext()) {
            if (iterator.next().equals(summary)) {
                if (summary.amount - remainingAmount <= 0) {
                    iterator.remove();
                    break;
                }
                iterator.set(new Summary(summary.amount - remainingAmount, summary.pricePerUnit));
                break;
            }
            iterator.remove();
        }
    }

    @Contract(" -> new")
    public @NotNull ShardData copy() {
        final List<Summary> sellSummary = new ArrayList<>();
        final List<Summary> buySummary = new ArrayList<>();

        for (Summary summary : sellSummary())
            sellSummary.add(new Summary(summary.amount, summary.pricePerUnit));

        for (Summary summary : buySummary())
            buySummary.add(new Summary(summary.amount, summary.pricePerUnit));

        return new ShardData(sellSummary, buySummary);
    }

    public record Summary(int amount, double pricePerUnit) {
    }
}
