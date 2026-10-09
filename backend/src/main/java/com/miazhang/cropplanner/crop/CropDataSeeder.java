package com.miazhang.cropplanner.crop;

import static com.miazhang.cropplanner.crop.Season.FALL;
import static com.miazhang.cropplanner.crop.Season.SPRING;
import static com.miazhang.cropplanner.crop.Season.SUMMER;

import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Loads a starting crop catalogue on first start. The numbers are starting values only:
 * every crop can be edited, added or removed through the API so players can enter the
 * prices from their own game.
 */
@Component
public class CropDataSeeder implements CommandLineRunner {

    private final CropRepository crops;

    public CropDataSeeder(CropRepository crops) {
        this.crops = crops;
    }

    @Override
    public void run(String... args) {
        if (crops.count() > 0) {
            return;
        }
        crops.saveAll(List.of(
                // Spring
                crop("Parsnip", 20, 35, 4, null, 1, SPRING),
                crop("Potato", 50, 80, 6, null, 1, SPRING),
                crop("Cauliflower", 80, 175, 12, null, 1, SPRING),
                crop("Green Bean", 60, 40, 10, 3, 1, SPRING),
                crop("Kale", 70, 110, 6, null, 1, SPRING),
                crop("Garlic", 40, 60, 4, null, 1, SPRING),
                crop("Rhubarb", 100, 220, 13, null, 1, SPRING),
                crop("Strawberry", 100, 120, 8, 4, 1, SPRING),
                // Summer
                crop("Melon", 80, 250, 12, null, 1, SUMMER),
                crop("Tomato", 50, 60, 11, 4, 1, SUMMER),
                crop("Blueberry", 80, 50, 13, 4, 3, SUMMER),
                crop("Hot Pepper", 40, 40, 5, 3, 1, SUMMER),
                crop("Radish", 40, 90, 6, null, 1, SUMMER),
                crop("Red Cabbage", 100, 260, 9, null, 1, SUMMER),
                crop("Starfruit", 400, 750, 13, null, 1, SUMMER),
                crop("Hops", 60, 25, 11, 1, 1, SUMMER),
                // Summer and fall
                crop("Wheat", 10, 25, 4, null, 1, SUMMER, FALL),
                crop("Corn", 150, 50, 14, 4, 1, SUMMER, FALL),
                crop("Sunflower", 200, 80, 8, null, 1, SUMMER, FALL),
                // Fall
                crop("Pumpkin", 100, 320, 13, null, 1, FALL),
                crop("Cranberries", 240, 75, 7, 5, 2, FALL),
                crop("Eggplant", 20, 60, 5, 5, 1, FALL),
                crop("Yam", 60, 160, 10, null, 1, FALL),
                crop("Grape", 60, 80, 10, 3, 1, FALL),
                crop("Bok Choy", 50, 80, 4, null, 1, FALL),
                crop("Amaranth", 70, 150, 7, null, 1, FALL),
                crop("Artichoke", 30, 160, 8, null, 1, FALL),
                crop("Beet", 20, 100, 6, null, 1, FALL)
        ));
    }

    private static Crop crop(String name, int seedPrice, int sellPrice, int growthDays,
                             Integer regrowDays, int harvestYield, Season... seasons) {
        return new Crop(name, seedPrice, sellPrice, growthDays, regrowDays, harvestYield, Set.of(seasons));
    }
}
