package com.miazhang.cropplanner.crop;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService crops;

    public CropController(CropService crops) {
        this.crops = crops;
    }

    /** Lists every crop, or only the crops that grow in the given season. */
    @GetMapping
    public List<Crop> list(@RequestParam(required = false) Season season) {
        return crops.list(season);
    }

    @GetMapping("/{id}")
    public Crop get(@PathVariable Long id) {
        return crops.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Crop create(@Valid @RequestBody CropRequest request) {
        return crops.create(request);
    }

    @PutMapping("/{id}")
    public Crop update(@PathVariable Long id, @Valid @RequestBody CropRequest request) {
        return crops.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        crops.delete(id);
    }
}
