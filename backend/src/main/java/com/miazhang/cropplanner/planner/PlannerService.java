package com.miazhang.cropplanner.planner;

import com.miazhang.cropplanner.common.NotFoundException;
import com.miazhang.cropplanner.crop.Crop;
import com.miazhang.cropplanner.crop.CropRepository;
import com.miazhang.cropplanner.crop.Season;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlannerService {

    private final CropRepository crops;
    private final ProfitCalculator calculator = new ProfitCalculator();

    public PlannerService(CropRepository crops) {
        this.crops = crops;
    }

    public CalculationResult calculate(Long cropId, Season season, int startDay, int tiles) {
        Crop crop = crops.findById(cropId)
                .orElseThrow(() -> new NotFoundException("Crop " + cropId + " was not found"));
        return calculate(crop, season, startDay, tiles);
    }

    public CalculationResult calculate(Crop crop, Season season, int startDay, int tiles) {
        int growingDays = calculator.growingDays(crop.getSeasons(), season, startDay);
        ProfitBreakdown b = calculator.calculate(crop.getSeedPrice(), crop.getSellPrice(), crop.getGrowthDays(),
                crop.getRegrowDays(), crop.getHarvestYield(), tiles, growingDays);
        return new CalculationResult(crop.getId(), crop.getName(), season, startDay, tiles, b.growingDays(),
                b.harvests(), b.seedsPerTile(), b.totalCost(), b.totalRevenue(), b.profit(), b.profitPerDay());
    }

    /** Every crop that grows in the season, most profitable first. */
    public List<CalculationResult> ranking(Season season, int startDay, int tiles) {
        return crops.findBySeason(season).stream()
                .map(crop -> calculate(crop, season, startDay, tiles))
                .sorted(Comparator.comparingLong(CalculationResult::profit).reversed()
                        .thenComparing(CalculationResult::cropName))
                .toList();
    }
}
