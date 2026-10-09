package com.miazhang.cropplanner.plan;

import com.miazhang.cropplanner.crop.Crop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_entries")
public class PlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private FarmPlan plan;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @Column(nullable = false)
    private int tiles;

    protected PlanEntry() {
        // required by JPA
    }

    PlanEntry(FarmPlan plan, Crop crop, int tiles) {
        this.plan = plan;
        this.crop = crop;
        this.tiles = tiles;
    }

    public Long getId() {
        return id;
    }

    public Crop getCrop() {
        return crop;
    }

    public int getTiles() {
        return tiles;
    }
}
