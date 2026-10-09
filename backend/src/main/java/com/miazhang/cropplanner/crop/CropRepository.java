package com.miazhang.cropplanner.crop;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CropRepository extends JpaRepository<Crop, Long> {

    @Query("select distinct c from Crop c join c.seasons s where s = :season order by c.name")
    List<Crop> findBySeason(@Param("season") Season season);

    List<Crop> findAllByOrderByNameAsc();

    Optional<Crop> findByNameIgnoreCase(String name);
}
