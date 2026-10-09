# Stardew Crop Planner

A full stack web app that helps you decide what to plant in Stardew Valley. Pick a season and a planting day, see which crops earn the most gold, build a plan and save it.

Fan made project. Not affiliated with ConcernedApe or Stardew Valley.

## Tech stack

| Layer | Technology |
| --- | --- |
| Front end | Angular 20, TypeScript, reactive forms, router, RxJS, HTML, CSS |
| Back end | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation |
| Database | H2 relational database through Hibernate (object relational mapping) |
| Testing | JUnit 5, MockMvc, Jasmine, Karma |
| DevOps | Docker, Docker Compose, GitHub Actions |

## Features

- **Editable crop catalogue**: starts with 28 crops, filterable by season. Add your own crops and edit or delete any crop, so the planner uses the prices from your game.
- **Profit ranking**: every crop for the chosen season and planting day, ranked by profit per tile.
- **Season planner**: a validated form to combine crops and tile counts into a plan.
- **Saved plans**: plans are stored in the database with cost, revenue and profit totals.

## Run it

### With Docker (only Docker needed)

```bash
docker compose up --build
```

Open http://localhost:4200. The API is on http://localhost:8080.

### Locally

Needs Java 17 or newer, Maven, and Node.js 20 or newer.

```bash
# terminal 1: back end on port 8080
cd backend
mvn spring-boot:run

# terminal 2: front end on port 4200 (forwards /api to the back end)
cd frontend
npm install
npm start
```

## Tests

```bash
cd backend && mvn verify          # 10 unit tests + 16 integration tests
cd frontend && npm run test:ci    # 28 component and service tests
```

GitHub Actions runs both suites and a production build on every push and pull request.

## REST API

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/crops?season=SUMMER` | List crops, optionally for one season |
| GET | `/api/crops/{id}` | One crop |
| POST | `/api/crops` | Add a crop |
| PUT | `/api/crops/{id}` | Change a crop's prices, growth time or seasons |
| DELETE | `/api/crops/{id}` | Delete a crop |
| POST | `/api/calculations` | Profit for one crop, season, start day and tile count |
| GET | `/api/rankings?season=FALL&startDay=1&tiles=1` | Crops ranked by profit |
| GET | `/api/plans` | Saved plans with totals |
| GET | `/api/plans/{id}` | One saved plan |
| POST | `/api/plans` | Save a plan |
| DELETE | `/api/plans/{id}` | Delete a plan |

Invalid input returns `400` with a JSON body listing the field errors. Unknown ids return `404`. A duplicate crop name, or deleting a crop that a saved plan still uses, returns `409`.

Example:

```bash
curl -X POST http://localhost:8080/api/calculations \
  -H "Content-Type: application/json" \
  -d '{"cropId": 1, "season": "SPRING", "startDay": 1, "tiles": 10}'
```

## How profit is calculated

The maths lives in `ProfitCalculator`, a plain Java class with no framework code.

- A season has 28 days. A crop planted on day `d` that takes `g` days is ready on day `d + g`.
- A crop that also grows in the next season keeps growing into it (corn, wheat, sunflower).
- Single harvest crops are replanted after each harvest, and each replant costs a seed.
- Regrowing crops are planted once and harvested again every regrow interval.
- Profit = harvests x items per harvest x sell price x tiles, minus seed cost.

Kept simple on purpose: no fertilizer, no farming level bonus, no random extra drops. The crop numbers that ship with the app are starting values, so correct them on the Crops page if your game shows something different.

## Project layout

```
backend/    Spring Boot API
  crop/       Crop entity, repository, service, controller, seed data
  planner/    profit calculator, calculation and ranking endpoints
  plan/       saved plans (entities, service, controller)
  common/     error handling
frontend/   Angular app
  crop-api.ts          HTTP service
  pages/crop-list/     catalogue page and the add or edit crop form
  pages/planner/       reactive form and ranking
  pages/plans/         saved plans page
```
