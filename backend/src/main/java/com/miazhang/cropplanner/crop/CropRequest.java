package com.miazhang.cropplanner.crop;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** Values a player types in when adding or correcting a crop. */
public record CropRequest(
        @NotBlank(message = "Give the crop a name")
        @Size(max = 40, message = "Name can be at most 40 characters") String name,
        @Min(value = 0, message = "Seed price cannot be negative")
        @Max(value = 1000000, message = "Seed price is too high") int seedPrice,
        @Min(value = 0, message = "Sell price cannot be negative")
        @Max(value = 1000000, message = "Sell price is too high") int sellPrice,
        @Min(value = 1, message = "Days to grow must be between 1 and 84")
        @Max(value = 84, message = "Days to grow must be between 1 and 84") int growthDays,
        @Min(value = 1, message = "Regrow days must be between 1 and 84")
        @Max(value = 84, message = "Regrow days must be between 1 and 84") Integer regrowDays,
        @Min(value = 1, message = "Items per harvest must be between 1 and 20")
        @Max(value = 20, message = "Items per harvest must be between 1 and 20") int harvestYield,
        @NotEmpty(message = "Choose at least one season") Set<Season> seasons) {
}
