import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CropApi } from './crop-api';
import { CropRequest, PlanRequest } from './models';

describe('CropApi', () => {
  let api: CropApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(CropApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('requests every crop when no season is given', () => {
    api.getCrops().subscribe();

    const req = http.expectOne('/api/crops');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('passes the season as a query parameter', () => {
    api.getCrops('FALL').subscribe();

    const req = http.expectOne('/api/crops?season=FALL');
    expect(req.request.params.get('season')).toBe('FALL');
    req.flush([]);
  });

  it('posts a new crop and puts changes to an existing one', () => {
    const crop: CropRequest = {
      name: 'Carrot', seedPrice: 30, sellPrice: 35, growthDays: 3, regrowDays: null, harvestYield: 1, seasons: ['SPRING'],
    };
    api.createCrop(crop).subscribe();
    api.updateCrop(4, crop).subscribe();

    const created = http.expectOne({ method: 'POST', url: '/api/crops' });
    expect(created.request.body).toEqual(crop);
    created.flush({});
    const updated = http.expectOne({ method: 'PUT', url: '/api/crops/4' });
    expect(updated.request.body).toEqual(crop);
    updated.flush({});
  });

  it('deletes a crop by id', () => {
    api.deleteCrop(4).subscribe();

    const req = http.expectOne('/api/crops/4');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('requests a ranking for a season, start day and tile count', () => {
    api.getRanking('SUMMER', 5, 10).subscribe();

    const req = http.expectOne('/api/rankings?season=SUMMER&startDay=5&tiles=10');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('posts a new plan', () => {
    const plan: PlanRequest = { name: 'Test', season: 'SPRING', startDay: 1, entries: [{ cropId: 1, tiles: 4 }] };
    api.createPlan(plan).subscribe();

    const req = http.expectOne('/api/plans');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(plan);
    req.flush({});
  });

  it('deletes a plan by id', () => {
    api.deletePlan(7).subscribe();

    const req = http.expectOne('/api/plans/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
