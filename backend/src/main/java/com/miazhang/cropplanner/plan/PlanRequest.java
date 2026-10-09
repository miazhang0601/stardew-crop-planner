package com.miazhang.cropplanner.plan;

import com.miazhang.cropplanner.crop.Season;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlanRequest(
        @NotBlank(message = "Give the plan a name")
        @Size(max = 60, message = "Name can be at most 60 characters") String name,
        @NotNull(message = "Choose a season") Season season,
        @Min(value = 1, message = "Start day must be between 1 and 28")
        @Max(value = 28, message = "Start day must be between 1 and 28") int startDay,
        @NotEmpty(message = "Add at least one crop") List<@Valid EntryRequest> entries) {

    public record EntryRequest(
            @NotNull(message = "Choose a crop") Long cropId,
            @Min(value = 1, message = "Tiles must be between 1 and 5000")
            @Max(value = 5000, message = "Tiles must be between 1 and 5000") int tiles) {
    }
}
