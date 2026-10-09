import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, combineLatest, debounceTime, distinctUntilChanged, filter, of, startWith, switchMap } from 'rxjs';
import { CropApi } from '../../crop-api';
import { ApiError, CalculationResult, Crop, PlanRequest, SEASONS, SEASON_LABELS, Season } from '../../models';

@Component({
  selector: 'app-planner',
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './planner.html',
})
export class Planner implements OnInit {
  private readonly api = inject(CropApi);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly seasons = SEASONS;
  protected readonly labels = SEASON_LABELS;
  protected readonly crops = signal<Crop[]>([]);
  protected readonly ranking = signal<CalculationResult[]>([]);
  protected readonly saving = signal(false);
  protected readonly serverError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(60)]],
    season: ['SPRING' as Season, Validators.required],
    startDay: [1, [Validators.required, Validators.min(1), Validators.max(28)]],
    entries: this.fb.array<FormGroup>([], Validators.required),
  });

  get entries(): FormArray<FormGroup> {
    return this.form.controls.entries;
  }

  ngOnInit(): void {
    const season$ = this.form.controls.season.valueChanges.pipe(startWith(this.form.controls.season.value));
    const startDay$ = this.form.controls.startDay.valueChanges.pipe(startWith(this.form.controls.startDay.value));

    // A crop from another season cannot stay in the plan, so changing season clears the rows.
    season$
      .pipe(
        distinctUntilChanged(),
        switchMap((season) => {
          this.entries.clear();
          return this.api.getCrops(season).pipe(catchError(() => of([] as Crop[])));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((crops) => this.crops.set(crops));

    // Refresh the profit ranking shortly after the season or start day stops changing.
    combineLatest([season$, startDay$])
      .pipe(
        debounceTime(250),
        filter(([, day]) => Number.isInteger(day) && day >= 1 && day <= 28),
        switchMap(([season, day]) => this.api.getRanking(season, day, 1).pipe(catchError(() => of([])))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((ranking) => this.ranking.set(ranking));
  }

  addEntry(cropId: number | null = null): void {
    this.entries.push(
      this.fb.group({
        cropId: [cropId, Validators.required],
        tiles: [10, [Validators.required, Validators.min(1), Validators.max(5000)]],
      }),
    );
  }

  removeEntry(index: number): void {
    this.entries.removeAt(index);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: PlanRequest = {
      name: value.name.trim(),
      season: value.season,
      startDay: value.startDay,
      entries: value.entries.map((e) => ({ cropId: Number(e['cropId']), tiles: Number(e['tiles']) })),
    };

    this.saving.set(true);
    this.serverError.set(null);
    this.api.createPlan(request).subscribe({
      next: () => this.router.navigate(['/plans']),
      error: (response: HttpErrorResponse) => {
        const body = response.error as ApiError | null;
        const fieldMessages = Object.values(body?.fieldErrors ?? {});
        this.serverError.set(fieldMessages[0] ?? body?.message ?? 'Could not save the plan. Is the API running?');
        this.saving.set(false);
      },
    });
  }
}
