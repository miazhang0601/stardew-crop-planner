package com.miazhang.cropplanner.planner;

import com.miazhang.cropplanner.crop.Season;

public record CalculationResult(
        Long cropId,
        String cropName,
        Season season,
        int startDay,
        int tiles,
        int growingDays,
        int harvests,
        int seedsPerTile,
        long totalCost,
        long totalRevenue,
        long profit,
        double profitPerDay) {
}
