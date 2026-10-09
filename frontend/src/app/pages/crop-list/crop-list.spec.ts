import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CropApi } from '../../crop-api';
import { Crop } from '../../models';
import { CropList } from './crop-list';

const CROPS: Crop[] = [
  { id: 1, name: 'Parsnip', seedPrice: 20, sellPrice: 35, growthDays: 4, regrowDays: null, harvestYield: 1, seasons: ['SPRING'] },
  { id: 2, name: 'Corn', seedPrice: 150, sellPrice: 50, growthDays: 14, regrowDays: 4, harvestYield: 1, seasons: ['SUMMER', 'FALL'] },
];

describe('CropList', () => {
  let api: jasmine.SpyObj<CropApi>;
  let fixture: ComponentFixture<CropList>;

  beforeEach(async () => {
    api = jasmine.createSpyObj<CropApi>('CropApi', ['getCrops', 'deleteCrop', 'updateCrop']);
    api.getCrops.and.returnValue(of(CROPS));
    api.deleteCrop.and.returnValue(of(undefined));
    api.updateCrop.and.callFake((id, crop) => of({ id, ...crop }));

    await TestBed.configureTestingModule({
      imports: [CropList],
      providers: [{ provide: CropApi, useValue: api }],
    }).compileComponents();
    fixture = TestBed.createComponent(CropList);
  });

  it('loads every crop on start and shows one row per crop', () => {
    fixture.detectChanges();

    expect(api.getCrops).toHaveBeenCalledWith(undefined);
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[1].textContent).toContain('every 4 days');
  });

  it('reloads with the chosen season when a filter button is clicked', () => {
    fixture.detectChanges();
    const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('.filters button'));

    buttons.find((b) => b.textContent?.includes('Fall'))!.click();
    fixture.detectChanges();

    expect(api.getCrops).toHaveBeenCalledWith('FALL');
    expect(buttons.find((b) => b.textContent?.includes('Fall'))!.classList).toContain('active');
  });

  it('shows an error message when the API call fails', () => {
    api.getCrops.and.returnValue(throwError(() => new Error('offline')));

    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.error')?.textContent).toContain('Could not load crops');
  });

  it('opens the form filled with the crop when Edit is clicked and reloads after saving', () => {
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('button[aria-label="Edit Corn"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    const form = fixture.nativeElement.querySelector('app-crop-form') as HTMLElement;
    expect(form.querySelector('h3')?.textContent).toContain('Edit Corn');
    expect((form.querySelector('input[formcontrolname="sellPrice"]') as HTMLInputElement).value).toBe('50');

    api.getCrops.calls.reset();
    (form.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(api.updateCrop).toHaveBeenCalledWith(2, jasmine.objectContaining({ name: 'Corn' }));
    expect(api.getCrops).toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('app-crop-form')).toBeNull();
  });

  it('removes a crop from the table after deleting it', () => {
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('button[aria-label="Delete Parsnip"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(api.deleteCrop).toHaveBeenCalledWith(1);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
  });

  it('shows why a crop could not be deleted', () => {
    api.deleteCrop.and.returnValue(throwError(() => ({ error: { status: 409, message: 'Parsnip is used in a saved plan. Delete that plan first.', fieldErrors: {} } })));
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('button[aria-label="Delete Parsnip"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.error')?.textContent).toContain('used in a saved plan');
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
  });
});
