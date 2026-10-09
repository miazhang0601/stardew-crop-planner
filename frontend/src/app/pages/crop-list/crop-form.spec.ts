import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CropApi } from '../../crop-api';
import { Crop } from '../../models';
import { CropForm } from './crop-form';

const CORN: Crop = {
  id: 2, name: 'Corn', seedPrice: 150, sellPrice: 50, growthDays: 14, regrowDays: 4, harvestYield: 1, seasons: ['SUMMER', 'FALL'],
};

describe('CropForm', () => {
  let api: jasmine.SpyObj<CropApi>;
  let fixture: ComponentFixture<CropForm>;
  let component: CropForm;

  beforeEach(async () => {
    api = jasmine.createSpyObj<CropApi>('CropApi', ['createCrop', 'updateCrop']);
    api.createCrop.and.callFake((crop) => of({ id: 99, ...crop }));
    api.updateCrop.and.callFake((id, crop) => of({ id, ...crop }));

    await TestBed.configureTestingModule({
      imports: [CropForm],
      providers: [{ provide: CropApi, useValue: api }],
    }).compileComponents();
    fixture = TestBed.createComponent(CropForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('is invalid until it has a name and at least one season', () => {
    expect(component.form.valid).toBeFalse();

    component.form.controls.name.setValue('Carrot');
    expect(component.form.valid).toBeFalse();

    component.form.controls.seasons.controls.SPRING.setValue(true);
    expect(component.form.valid).toBeTrue();
  });

  it('does not call the API while the form is invalid', () => {
    component.save();

    expect(api.createCrop).not.toHaveBeenCalled();
  });

  it('creates a crop from the typed values and emits it', () => {
    const saved = jasmine.createSpy('saved');
    component.saved.subscribe(saved);
    component.form.patchValue({ name: ' Carrot ', seedPrice: 30, sellPrice: 35, growthDays: 3, seasons: { SPRING: true } });

    component.save();

    expect(api.createCrop).toHaveBeenCalledWith({
      name: 'Carrot', seedPrice: 30, sellPrice: 35, growthDays: 3, regrowDays: null, harvestYield: 1, seasons: ['SPRING'],
    });
    expect(saved).toHaveBeenCalledWith(jasmine.objectContaining({ id: 99, name: 'Carrot' }));
  });

  it('fills the form from the crop being edited and saves it as an update', () => {
    component.crop = CORN;
    fixture.detectChanges();
    expect(component.form.controls.sellPrice.value).toBe(50);
    expect(component.form.controls.seasons.value).toEqual({ SPRING: false, SUMMER: true, FALL: true });

    component.form.controls.sellPrice.setValue(75);
    component.save();

    expect(api.updateCrop).toHaveBeenCalledWith(2, jasmine.objectContaining({ name: 'Corn', sellPrice: 75, regrowDays: 4 }));
    expect(api.createCrop).not.toHaveBeenCalled();
  });

  it('shows the message from the API when saving fails', () => {
    api.createCrop.and.returnValue(throwError(() => ({ error: { status: 409, message: 'A crop named Carrot already exists', fieldErrors: {} } })));
    component.form.patchValue({ name: 'Carrot', seasons: { SPRING: true } });

    component.save();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.error')?.textContent).toContain('already exists');
  });
});
