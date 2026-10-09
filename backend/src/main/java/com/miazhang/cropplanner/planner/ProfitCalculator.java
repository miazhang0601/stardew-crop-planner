package com.miazhang.cropplanner.planner;

import com.miazhang.cropplanner.crop.Season;
import java.util.Set;

/**
 * Pure profit maths with no framework dependencies, so it can be unit tested on its own.
 *
 * <p>Rules it follows:
 * <ul>
 *   <li>A season has 28 days. A crop planted on day d with a growth time of g is ready on day d + g.</li>
 *   <li>A crop keeps growing into the next season when it also grows in that season (corn, wheat, sunflower).</li>
 *   <li>Single harvest crops are replanted right after each harvest, which costs a new seed.</li>
 *   <li>Regrowing crops are planted once and harvested again every regrow interval.</li>
 * </ul>
 */
public class ProfitCalculator {

    public static final int DAYS_IN_SEASON = 28;
    public static final int MAX_TILES = 5000;

    /** Days a crop has to grow when planted on the given day, counting any following seasons it also grows in. */
    public int growingDays(Set<Season> cropSeasons, Season season, int startDay) {
        if (startDay < 1 || startDay > DAYS_IN_SEASON) {
            throw new IllegalArgumentException("Start day must be between 1 and " + DAYS_IN_SEASON);
        }
        if (!cropSeasons.contains(season)) {
            throw new IllegalArgumentException("This crop does not grow in " + season);
        }
        int days = DAYS_IN_SEASON - startDay;
        Season[] order = Season.values();
        for (int i = season.ordinal() + 1; i < order.length && cropSeasons.contains(order[i]); i++) {
            days += DAYS_IN_SEASON;
        }
        return days;
    }

    public ProfitBreakdown calculate(int seedPrice, int sellPrice, int growthDays, Integer regrowDays,
                                     int harvestYield, int tiles, int growingDays) {
        if (tiles < 1 || tiles > MAX_TILES) {
            throw new IllegalArgumentException("Tiles must be between 1 and " + MAX_TILES);
        }
        if (growthDays < 1) {
            throw new IllegalArgumentException("Growth days must be at least 1");
        }

        int harvests;
        int seedsPerTile;
        if (growingDays < growthDays) {
            // Not enough time left for even one harvest, so nothing is planted.
            harvests = 0;
            seedsPerTile = 0;
        } else if (regrowDays != null && regrowDays > 0) {
            harvests = 1 + (growingDays - growthDays) / regrowDays;
            seedsPerTile = 1;
        } else {
            harvests = growingDays / growthDays;
            seedsPerTile = harvests;
        }

        long totalCost = (long) seedsPerTile * seedPrice * tiles;
        long totalRevenue = (long) harvests * harvestYield * sellPrice * tiles;
        long profit = totalRevenue - totalCost;
        double profitPerDay = growingDays == 0 ? 0 : Math.round(profit * 100.0 / growingDays) / 100.0;

        return new ProfitBreakdown(growingDays, harvests, seedsPerTile, totalCost, totalRevenue, profit, profitPerDay);
    }
}
