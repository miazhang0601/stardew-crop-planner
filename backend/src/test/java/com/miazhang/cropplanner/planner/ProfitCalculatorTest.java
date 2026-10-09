package com.miazhang.cropplanner.planner;

import static com.miazhang.cropplanner.crop.Season.FALL;
import static com.miazhang.cropplanner.crop.Season.SPRING;
import static com.miazhang.cropplanner.crop.Season.SUMMER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ProfitCalculatorTest {

    private final ProfitCalculator calculator = new ProfitCalculator();

    @Test
    void growingDaysCountsTheRestOfTheSeason() {
        assertEquals(27, calculator.growingDays(Set.of(SPRING), SPRING, 1));
        assertEquals(0, calculator.growingDays(Set.of(SPRING), SPRING, 28));
    }

    @Test
    void growingDaysCarriesIntoTheNextSeasonForMultiSeasonCrops() {
        assertEquals(55, calculator.growingDays(Set.of(SUMMER, FALL), SUMMER, 1));
        assertEquals(27, calculator.growingDays(Set.of(SUMMER, FALL), FALL, 1));
    }

    @Test
    void growingDaysRejectsACropOutOfSeason() {
        assertThrows(IllegalArgumentException.class, () -> calculator.growingDays(Set.of(FALL), SPRING, 1));
    }

    @Test
    void growingDaysRejectsADayOutsideTheSeason() {
        assertThrows(IllegalArgumentException.class, () -> calculator.growingDays(Set.of(SPRING), SPRING, 0));
        assertThrows(IllegalArgumentException.class, () -> calculator.growingDays(Set.of(SPRING), SPRING, 29));
    }

    @Test
    void singleHarvestCropIsReplantedAfterEveryHarvest() {
        // Parsnip: 20g seed, 35g sale, 4 days. 27 days allows 6 harvests on 10 tiles.
        ProfitBreakdown result = calculator.calculate(20, 35, 4, null, 1, 10, 27);

        assertEquals(6, result.harvests());
        assertEquals(6, result.seedsPerTile());
        assertEquals(1200, result.totalCost());
        assertEquals(2100, result.totalRevenue());
        assertEquals(900, result.profit());
        assertEquals(33.33, result.profitPerDay());
    }

    @Test
    void regrowingCropIsPlantedOnceAndUsesItsYield() {
        // Blueberry: 80g seed, 50g sale, 13 days, regrows every 4 days, 3 berries per harvest.
        ProfitBreakdown result = calculator.calculate(80, 50, 13, 4, 3, 1, 27);

        assertEquals(4, result.harvests());
        assertEquals(1, result.seedsPerTile());
        assertEquals(80, result.totalCost());
        assertEquals(600, result.totalRevenue());
        assertEquals(520, result.profit());
    }

    @Test
    void multiSeasonRegrowingCropKeepsProducing() {
        // Corn planted on Summer 1 has 55 growing days: 14 to mature, then a harvest every 4 days.
        ProfitBreakdown result = calculator.calculate(150, 50, 14, 4, 1, 1, 55);

        assertEquals(11, result.harvests());
        assertEquals(400, result.profit());
    }

    @Test
    void nothingIsPlantedWhenTheCropCannotMatureInTime() {
        // Pumpkin needs 13 days but only 8 are left.
        ProfitBreakdown result = calculator.calculate(100, 320, 13, null, 1, 5, 8);

        assertEquals(0, result.harvests());
        assertEquals(0, result.totalCost());
        assertEquals(0, result.profit());
    }

    @Test
    void zeroGrowingDaysDoesNotDivideByZero() {
        assertEquals(0.0, calculator.calculate(20, 35, 4, null, 1, 1, 0).profitPerDay());
    }

    @Test
    void tilesMustBeWithinRange() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, 35, 4, null, 1, 0, 27));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, 35, 4, null, 1, 5001, 27));
    }
}
