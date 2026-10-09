package com.miazhang.cropplanner.plan;

import com.miazhang.cropplanner.crop.Season;
import com.miazhang.cropplanner.planner.CalculationResult;
import java.time.Instant;
import java.util.List;

public record PlanResponse(
        Long id,
        String name,
        Season season,
        int startDay,
        Instant createdAt,
        List<CalculationResult> entries,
        long totalCost,
        long totalRevenue,
        long totalProfit) {
}
