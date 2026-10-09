package com.miazhang.cropplanner.crop;

import com.miazhang.cropplanner.common.ConflictException;
import com.miazhang.cropplanner.common.NotFoundException;
import com.miazhang.cropplanner.plan.FarmPlanRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CropService {

    private final CropRepository crops;
    private final FarmPlanRepository plans;

    public CropService(CropRepository crops, FarmPlanRepository plans) {
        this.crops = crops;
        this.plans = plans;
    }

    @Transactional(readOnly = true)
    public List<Crop> list(Season season) {
        return season == null ? crops.findAllByOrderByNameAsc() : crops.findBySeason(season);
    }

    @Transactional(readOnly = true)
    public Crop get(Long id) {
        return crops.findById(id).orElseThrow(() -> new NotFoundException("Crop " + id + " was not found"));
    }

    public Crop create(CropRequest request) {
        String name = request.name().trim();
        if (crops.findByNameIgnoreCase(name).isPresent()) {
            throw new ConflictException("A crop named " + name + " already exists");
        }
        return crops.save(new Crop(name, request.seedPrice(), request.sellPrice(), request.growthDays(),
                request.regrowDays(), request.harvestYield(), request.seasons()));
    }

    public Crop update(Long id, CropRequest request) {
        Crop crop = get(id);
        String name = request.name().trim();
        crops.findByNameIgnoreCase(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("A crop named " + name + " already exists");
                });
        // A saved plan must stay valid, so a season it relies on cannot be removed from the crop.
        for (Season used : plans.findSeasonsOfPlansUsingCrop(id)) {
            if (!request.seasons().contains(used)) {
                throw new ConflictException(crop.getName() + " is used in a saved " + used
                        + " plan, so that season cannot be removed");
            }
        }
        crop.update(name, request.seedPrice(), request.sellPrice(), request.growthDays(),
                request.regrowDays(), request.harvestYield(), request.seasons());
        return crop;
    }

    public void delete(Long id) {
        Crop crop = get(id);
        if (!plans.findSeasonsOfPlansUsingCrop(id).isEmpty()) {
            throw new ConflictException(crop.getName() + " is used in a saved plan. Delete that plan first.");
        }
        crops.delete(crop);
    }
}
