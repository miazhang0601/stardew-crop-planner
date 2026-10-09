package com.miazhang.cropplanner.crop;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int seedPrice;

    @Column(nullable = false)
    private int sellPrice;

    /** Days from planting to the first harvest. */
    @Column(nullable = false)
    private int growthDays;

    /** Days between harvests for crops that keep producing. Null means the crop is harvested once. */
    private Integer regrowDays;

    /** Items produced per harvest (blueberries give 3, cranberries give 2). */
    @Column(nullable = false)
    private int harvestYield;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "crop_seasons", joinColumns = @JoinColumn(name = "crop_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "season", nullable = false)
    private Set<Season> seasons = new LinkedHashSet<>();

    protected Crop() {
        // required by JPA
    }

    public Crop(String name, int seedPrice, int sellPrice, int growthDays,
                Integer regrowDays, int harvestYield, Set<Season> seasons) {
        this.name = name;
        this.seedPrice = seedPrice;
        this.sellPrice = sellPrice;
        this.growthDays = growthDays;
        this.regrowDays = regrowDays;
        this.harvestYield = harvestYield;
        this.seasons = new LinkedHashSet<>(seasons);
    }

    /** Replaces every editable value, used when a player corrects a crop. */
    public void update(String name, int seedPrice, int sellPrice, int growthDays,
                       Integer regrowDays, int harvestYield, Set<Season> seasons) {
        this.name = name;
        this.seedPrice = seedPrice;
        this.sellPrice = sellPrice;
        this.growthDays = growthDays;
        this.regrowDays = regrowDays;
        this.harvestYield = harvestYield;
        this.seasons.clear();
        this.seasons.addAll(seasons);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeedPrice() {
        return seedPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public int getGrowthDays() {
        return growthDays;
    }

    public Integer getRegrowDays() {
        return regrowDays;
    }

    public int getHarvestYield() {
        return harvestYield;
    }

    public Set<Season> getSeasons() {
        return seasons;
    }
}
