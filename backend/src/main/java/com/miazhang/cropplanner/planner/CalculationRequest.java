package com.miazhang.cropplanner.planner;

import com.miazhang.cropplanner.crop.Season;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CalculationRequest(
        @NotNull(message = "Choose a crop") Long cropId,
        @NotNull(message = "Choose a season") Season season,
        @Min(value = 1, message = "Start day must be between 1 and 28")
        @Max(value = 28, message = "Start day must be between 1 and 28") int startDay,
        @Min(value = 1, message = "Tiles must be between 1 and 5000")
        @Max(value = 5000, message = "Tiles must be between 1 and 5000") int tiles) {
}
