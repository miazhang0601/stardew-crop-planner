import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { CropApi } from '../../crop-api';
import { ApiError, Crop, CropRequest, SEASONS, SEASON_LABELS } from '../../models';

function atLeastOneSeason(group: AbstractControl): ValidationErrors | null {
  return Object.values(group.value as Record<string, boolean>).some(Boolean) ? null : { noSeason: true };
}

/** Form for typing in a new crop or correcting the numbers of an existing one. */
@Component({
  selector: 'app-crop-form',
  imports: [ReactiveFormsModule],
  templateUrl: './crop-form.html',
})
export class CropForm {
  private readonly api = inject(CropApi);
  private readonly fb = inject(FormBuilder);

  protected readonly seasons = SEASONS;
  protected readonly labels = SEASON_LABELS;
  protected readonly saving = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly editing = signal<Crop | null>(null);

  @Output() readonly saved = new EventEmitter<Crop>();
  @Output() readonly cancelled = new EventEmitter<void>();

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(40)]],
    seedPrice: [0, [Validators.required, Validators.min(0)]],
    sellPrice: [0, [Validators.required, Validators.min(0)]],
    growthDays: [4, [Validators.required, Validators.min(1), Validators.max(84)]],
    regrowDays: [null as number | null, [Validators.min(1), Validators.max(84)]],
    harvestYield: [1, [Validators.required, Validators.min(1), Validators.max(20)]],
    seasons: this.fb.nonNullable.group({ SPRING: false, SUMMER: false, FALL: false }, { validators: atLeastOneSeason }),
  });

  /** The crop to edit, or null to add a new one. */
  @Input() set crop(crop: Crop | null) {
    this.editing.set(crop);
    this.serverError.set(null);
    if (crop) {
      this.form.reset({
        name: crop.name,
        seedPrice: crop.seedPrice,
        sellPrice: crop.sellPrice,
        growthDays: crop.growthDays,
        regrowDays: crop.regrowDays,
        harvestYield: crop.harvestYield,
        seasons: {
          SPRING: crop.seasons.includes('SPRING'),
          SUMMER: crop.seasons.includes('SUMMER'),
          FALL: crop.seasons.includes('FALL'),
        },
      });
    } else {
      this.form.reset();
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: CropRequest = {
      name: value.name.trim(),
      seedPrice: value.seedPrice,
      sellPrice: value.sellPrice,
      growthDays: value.growthDays,
      regrowDays: value.regrowDays || null,
      harvestYield: value.harvestYield,
      seasons: SEASONS.filter((season) => value.seasons[season]),
    };
    const existing = this.editing();
    const call = existing ? this.api.updateCrop(existing.id, request) : this.api.createCrop(request);

    this.saving.set(true);
    this.serverError.set(null);
    call.subscribe({
      next: (crop) => {
        this.saving.set(false);
        this.saved.emit(crop);
      },
      error: (response: HttpErrorResponse) => {
        const body = response.error as ApiError | null;
        const fieldMessages = Object.values(body?.fieldErrors ?? {});
        this.serverError.set(fieldMessages[0] ?? body?.message ?? 'Could not save the crop. Is the API running?');
        this.saving.set(false);
      },
    });
  }
}
