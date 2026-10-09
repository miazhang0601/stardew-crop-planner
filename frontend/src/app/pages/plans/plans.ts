import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CropApi } from '../../crop-api';
import { Plan, SEASON_LABELS } from '../../models';

@Component({
  selector: 'app-plans',
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './plans.html',
})
export class Plans implements OnInit {
  private readonly api = inject(CropApi);

  protected readonly labels = SEASON_LABELS;
  protected readonly plans = signal<Plan[]>([]);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getPlans().subscribe({
      next: (plans) => this.plans.set(plans),
      error: () => this.error.set('Could not load plans. Is the API running on port 8080?'),
    });
  }

  remove(plan: Plan): void {
    this.api.deletePlan(plan.id).subscribe({
      next: () => this.plans.update((plans) => plans.filter((p) => p.id !== plan.id)),
      error: () => this.error.set('Could not delete the plan.'),
    });
  }
}
