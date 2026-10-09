import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CalculationResult, Crop, CropRequest, Plan, PlanRequest, Season } from './models';

/** Talks to the Spring Boot REST API. */
@Injectable({ providedIn: 'root' })
export class CropApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  getCrops(season?: Season): Observable<Crop[]> {
    const params = season ? new HttpParams().set('season', season) : undefined;
    return this.http.get<Crop[]>(`${this.baseUrl}/crops`, { params });
  }

  createCrop(crop: CropRequest): Observable<Crop> {
    return this.http.post<Crop>(`${this.baseUrl}/crops`, crop);
  }

  updateCrop(id: number, crop: CropRequest): Observable<Crop> {
    return this.http.put<Crop>(`${this.baseUrl}/crops/${id}`, crop);
  }

  deleteCrop(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/crops/${id}`);
  }

  getRanking(season: Season, startDay: number, tiles = 1): Observable<CalculationResult[]> {
    const params = new HttpParams().set('season', season).set('startDay', startDay).set('tiles', tiles);
    return this.http.get<CalculationResult[]>(`${this.baseUrl}/rankings`, { params });
  }

  getPlans(): Observable<Plan[]> {
    return this.http.get<Plan[]>(`${this.baseUrl}/plans`);
  }

  createPlan(plan: PlanRequest): Observable<Plan> {
    return this.http.post<Plan>(`${this.baseUrl}/plans`, plan);
  }

  deletePlan(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/plans/${id}`);
  }
}
