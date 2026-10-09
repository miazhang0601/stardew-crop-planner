package com.miazhang.cropplanner.planner;

/** Result of the profit maths for one crop on a number of tiles. */
public record ProfitBreakdown(
        int growingDays,
        int harvests,
        int seedsPerTile,
        long totalCost,
        long totalRevenue,
        long profit,
        double profitPerDay) {
}
