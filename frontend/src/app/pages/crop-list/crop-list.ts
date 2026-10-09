import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { CropApi } from '../../crop-api';
import { ApiError, Crop, SEASONS, SEASON_LABELS, Season } from '../../models';
import { CropForm } from './crop-form';

@Component({
  selector: 'app-crop-list',
  imports: [CropForm],
  templateUrl: './crop-list.html',
})
export class CropList implements OnInit {
  private readonly api = inject(CropApi);

  protected readonly seasons = SEASONS;
  protected readonly labels = SEASON_LABELS;
  protected readonly selected = signal<Season | null>(null);
  protected readonly crops = signal<Crop[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Whether the add or edit form is open, and the crop being edited (null when adding). */
  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Crop | null>(null);

  ngOnInit(): void {
    this.load(null);
  }

  load(season: Season | null): void {
    this.selected.set(season);
    this.loading.set(true);
    this.error.set(null);
    this.api.getCrops(season ?? undefined).subscribe({
      next: (crops) => {
        this.crops.set(crops);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load crops. Is the API running on port 8080?');
        this.loading.set(false);
      },
    });
  }

  openForm(crop: Crop | null): void {
    this.editing.set(crop);
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  onSaved(): void {
    this.closeForm();
    this.load(this.selected());
  }

  remove(crop: Crop): void {
    this.api.deleteCrop(crop.id).subscribe({
      next: () => this.crops.update((crops) => crops.filter((c) => c.id !== crop.id)),
      error: (response: HttpErrorResponse) =>
        this.error.set((response.error as ApiError | null)?.message ?? 'Could not delete the crop.'),
    });
  }
}
