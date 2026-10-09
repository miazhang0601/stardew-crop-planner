package com.miazhang.cropplanner.plan;

import com.miazhang.cropplanner.crop.Season;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FarmPlanRepository extends JpaRepository<FarmPlan, Long> {

    List<FarmPlan> findAllByOrderByCreatedAtDesc();

    /** Seasons of every saved plan that contains the crop. Empty when no plan uses it. */
    @Query("select distinct p.season from FarmPlan p join p.entries e where e.crop.id = :cropId")
    List<Season> findSeasonsOfPlansUsingCrop(@Param("cropId") Long cropId);
}
