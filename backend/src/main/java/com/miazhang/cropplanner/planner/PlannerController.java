package com.miazhang.cropplanner.planner;

import com.miazhang.cropplanner.crop.Season;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlannerController {

    private final PlannerService planner;

    public PlannerController(PlannerService planner) {
        this.planner = planner;
    }

    @PostMapping("/calculations")
    public CalculationResult calculate(@Valid @RequestBody CalculationRequest request) {
        return planner.calculate(request.cropId(), request.season(), request.startDay(), request.tiles());
    }

    @GetMapping("/rankings")
    public List<CalculationResult> ranking(@RequestParam Season season,
                                           @RequestParam(defaultValue = "1") int startDay,
                                           @RequestParam(defaultValue = "1") int tiles) {
        return planner.ranking(season, startDay, tiles);
    }
}
