package com.miazhang.cropplanner.plan;

import com.miazhang.cropplanner.common.NotFoundException;
import com.miazhang.cropplanner.crop.Crop;
import com.miazhang.cropplanner.crop.CropRepository;
import com.miazhang.cropplanner.planner.CalculationResult;
import com.miazhang.cropplanner.planner.PlannerService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanService {

    private final FarmPlanRepository plans;
    private final CropRepository crops;
    private final PlannerService planner;

    public PlanService(FarmPlanRepository plans, CropRepository crops, PlannerService planner) {
        this.plans = plans;
        this.crops = crops;
        this.planner = planner;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> list() {
        return plans.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse get(Long id) {
        return toResponse(find(id));
    }

    public PlanResponse create(PlanRequest request) {
        FarmPlan plan = new FarmPlan(request.name().trim(), request.season(), request.startDay());
        for (PlanRequest.EntryRequest entry : request.entries()) {
            Crop crop = crops.findById(entry.cropId())
                    .orElseThrow(() -> new NotFoundException("Crop " + entry.cropId() + " was not found"));
            if (!crop.getSeasons().contains(request.season())) {
                throw new IllegalArgumentException(crop.getName() + " does not grow in " + request.season());
            }
            plan.addEntry(crop, entry.tiles());
        }
        return toResponse(plans.save(plan));
    }

    public void delete(Long id) {
        plans.delete(find(id));
    }

    private FarmPlan find(Long id) {
        return plans.findById(id).orElseThrow(() -> new NotFoundException("Plan " + id + " was not found"));
    }

    private PlanResponse toResponse(FarmPlan plan) {
        List<CalculationResult> entries = plan.getEntries().stream()
                .map(e -> planner.calculate(e.getCrop(), plan.getSeason(), plan.getStartDay(), e.getTiles()))
                .toList();
        long cost = entries.stream().mapToLong(CalculationResult::totalCost).sum();
        long revenue = entries.stream().mapToLong(CalculationResult::totalRevenue).sum();
        return new PlanResponse(plan.getId(), plan.getName(), plan.getSeason(), plan.getStartDay(),
                plan.getCreatedAt(), entries, cost, revenue, revenue - cost);
    }
}
