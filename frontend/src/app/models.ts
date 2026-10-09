export type Season = 'SPRING' | 'SUMMER' | 'FALL';

export const SEASONS: readonly Season[] = ['SPRING', 'SUMMER', 'FALL'];

export const SEASON_LABELS: Record<Season, string> = {
  SPRING: '🌱 Spring',
  SUMMER: '☀️ Summer',
  FALL: '🍂 Fall',
};

export interface Crop {
  id: number;
  name: string;
  seedPrice: number;
  sellPrice: number;
  growthDays: number;
  regrowDays: number | null;
  harvestYield: number;
  seasons: Season[];
}

/** What the player types in when adding or correcting a crop. */
export type CropRequest = Omit<Crop, 'id'>;

export interface CalculationResult {
  cropId: number;
  cropName: string;
  season: Season;
  startDay: number;
  tiles: number;
  growingDays: number;
  harvests: number;
  seedsPerTile: number;
  totalCost: number;
  totalRevenue: number;
  profit: number;
  profitPerDay: number;
}

export interface PlanEntryRequest {
  cropId: number;
  tiles: number;
}

export interface PlanRequest {
  name: string;
  season: Season;
  startDay: number;
  entries: PlanEntryRequest[];
}

export interface Plan {
  id: number;
  name: string;
  season: Season;
  startDay: number;
  createdAt: string;
  entries: CalculationResult[];
  totalCost: number;
  totalRevenue: number;
  totalProfit: number;
}

export interface ApiError {
  status: number;
  message: string;
  fieldErrors: Record<string, string>;
}
