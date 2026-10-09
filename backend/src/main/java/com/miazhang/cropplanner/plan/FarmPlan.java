package com.miazhang.cropplanner.plan;

import com.miazhang.cropplanner.crop.Crop;
import com.miazhang.cropplanner.crop.Season;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "farm_plans")
public class FarmPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season;

    @Column(nullable = false)
    private int startDay;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<PlanEntry> entries = new ArrayList<>();

    protected FarmPlan() {
        // required by JPA
    }

    public FarmPlan(String name, Season season, int startDay) {
        this.name = name;
        this.season = season;
        this.startDay = startDay;
        this.createdAt = Instant.now();
    }

    public void addEntry(Crop crop, int tiles) {
        entries.add(new PlanEntry(this, crop, tiles));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Season getSeason() {
        return season;
    }

    public int getStartDay() {
        return startDay;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PlanEntry> getEntries() {
        return entries;
    }
}
