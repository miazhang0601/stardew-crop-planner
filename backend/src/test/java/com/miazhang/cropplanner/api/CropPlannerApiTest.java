package com.miazhang.cropplanner.api;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miazhang.cropplanner.crop.Crop;
import com.miazhang.cropplanner.crop.CropRepository;
import com.miazhang.cropplanner.plan.FarmPlanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests that run the whole stack: controller, service, JPA and an in memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class CropPlannerApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CropRepository crops;

    @Autowired
    private FarmPlanRepository plans;

    @Autowired
    private ObjectMapper json;

    /** Tests share one database, so anything a test adds is removed again. */
    @AfterEach
    void removeTestData() {
        plans.deleteAll();
        crops.findAll().stream()
                .filter(c -> c.getName().startsWith("Test "))
                .forEach(crops::delete);
    }

    private long createCrop(String body) throws Exception {
        String created = mvc.perform(post("/api/crops").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(created).get("id").asLong();
    }

    private static final String TEST_BERRY = """
            {"name": "Test Berry", "seedPrice": 100, "sellPrice": 500, "growthDays": 9,
             "regrowDays": null, "harvestYield": 1, "seasons": ["FALL"]}
            """;

    private long cropId(String name) {
        return crops.findAll().stream()
                .filter(c -> c.getName().equals(name))
                .map(Crop::getId)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void listsEveryCrop() throws Exception {
        mvc.perform(get("/api/crops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(28)))
                .andExpect(jsonPath("$[0].name", is("Amaranth")));
    }

    @Test
    void filtersCropsBySeason() throws Exception {
        mvc.perform(get("/api/crops").param("season", "SUMMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[*].name", hasItem("Corn")))
                .andExpect(jsonPath("$[*].seasons", everyItem(hasItem("SUMMER"))));
    }

    @Test
    void returns404ForAnUnknownCrop() throws Exception {
        mvc.perform(get("/api/crops/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void rejectsAnUnknownSeason() throws Exception {
        mvc.perform(get("/api/crops").param("season", "WINTER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculatesProfitForACrop() throws Exception {
        String body = """
                {"cropId": %d, "season": "SPRING", "startDay": 1, "tiles": 10}
                """.formatted(cropId("Parsnip"));

        mvc.perform(post("/api/calculations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cropName", is("Parsnip")))
                .andExpect(jsonPath("$.harvests", is(6)))
                .andExpect(jsonPath("$.profit", is(900)));
    }

    @Test
    void rejectsACalculationWithInvalidInput() throws Exception {
        String body = """
                {"cropId": %d, "season": "SPRING", "startDay": 40, "tiles": 0}
                """.formatted(cropId("Parsnip"));

        mvc.perform(post("/api/calculations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.startDay").exists())
                .andExpect(jsonPath("$.fieldErrors.tiles").exists());
    }

    @Test
    void rejectsACropOutOfSeason() throws Exception {
        String body = """
                {"cropId": %d, "season": "SPRING", "startDay": 1, "tiles": 1}
                """.formatted(cropId("Pumpkin"));

        mvc.perform(post("/api/calculations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ranksCropsByProfit() throws Exception {
        // From Fall 1 on one tile the regrowing cranberry beats every single harvest crop.
        mvc.perform(get("/api/rankings").param("season", "FALL").param("startDay", "1").param("tiles", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[0].cropName", is("Cranberries")))
                .andExpect(jsonPath("$[0].profit", is(510)));
    }

    @Test
    void savesListsAndDeletesAPlan() throws Exception {
        String body = """
                {"name": "First summer", "season": "SUMMER", "startDay": 1,
                 "entries": [{"cropId": %d, "tiles": 20}, {"cropId": %d, "tiles": 5}]}
                """.formatted(cropId("Blueberry"), cropId("Corn"));

        String created = mvc.perform(post("/api/plans").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("First summer")))
                .andExpect(jsonPath("$.entries", hasSize(2)))
                // Blueberry 520 x 20 tiles + Corn 400 x 5 tiles
                .andExpect(jsonPath("$.totalProfit", is(12400)))
                .andReturn().getResponse().getContentAsString();

        JsonNode plan = json.readTree(created);
        long id = plan.get("id").asLong();

        mvc.perform(get("/api/plans/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)));

        mvc.perform(delete("/api/plans/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/plans/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsAPlanWithoutEntries() throws Exception {
        String body = """
                {"name": "", "season": "FALL", "startDay": 1, "entries": []}
                """;

        mvc.perform(post("/api/plans").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.entries").exists());
    }

    @Test
    void addsACropTypedInByThePlayerAndRanksIt() throws Exception {
        long id = createCrop(TEST_BERRY);

        mvc.perform(get("/api/crops/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Berry")))
                .andExpect(jsonPath("$.seasons", hasItem("FALL")));

        // 27 days allow 3 harvests: 3 x 500 revenue minus 3 x 100 seeds = 1200, the best fall crop.
        mvc.perform(get("/api/rankings").param("season", "FALL"))
                .andExpect(jsonPath("$[0].cropName", is("Test Berry")))
                .andExpect(jsonPath("$[0].profit", is(1200)));
    }

    @Test
    void editingAPriceChangesTheCalculation() throws Exception {
        long id = createCrop(TEST_BERRY);
        String cheaper = TEST_BERRY.replace("\"sellPrice\": 500", "\"sellPrice\": 200");

        mvc.perform(put("/api/crops/" + id).contentType(MediaType.APPLICATION_JSON).content(cheaper))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellPrice", is(200)));

        String body = """
                {"cropId": %d, "season": "FALL", "startDay": 1, "tiles": 1}
                """.formatted(id);
        mvc.perform(post("/api/calculations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.profit", is(300)));
    }

    @Test
    void rejectsACropWithInvalidValues() throws Exception {
        String body = """
                {"name": " ", "seedPrice": -5, "sellPrice": 10, "growthDays": 0,
                 "regrowDays": null, "harvestYield": 1, "seasons": []}
                """;

        mvc.perform(post("/api/crops").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.seedPrice").exists())
                .andExpect(jsonPath("$.fieldErrors.growthDays").exists())
                .andExpect(jsonPath("$.fieldErrors.seasons").exists());
    }

    @Test
    void rejectsADuplicateCropName() throws Exception {
        String body = TEST_BERRY.replace("Test Berry", "parsnip");

        mvc.perform(post("/api/crops").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deletesACropThatNoPlanUses() throws Exception {
        long id = createCrop(TEST_BERRY);

        mvc.perform(delete("/api/crops/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/crops/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void keepsACropThatASavedPlanStillUses() throws Exception {
        long id = createCrop(TEST_BERRY);
        String plan = """
                {"name": "Berry fall", "season": "FALL", "startDay": 1, "entries": [{"cropId": %d, "tiles": 4}]}
                """.formatted(id);
        mvc.perform(post("/api/plans").contentType(MediaType.APPLICATION_JSON).content(plan))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/crops/" + id)).andExpect(status().isConflict());

        // Moving the crop out of the plan's season would break the plan, so that is refused too.
        String springOnly = TEST_BERRY.replace("FALL", "SPRING");
        mvc.perform(put("/api/crops/" + id).contentType(MediaType.APPLICATION_JSON).content(springOnly))
                .andExpect(status().isConflict());
    }
}
