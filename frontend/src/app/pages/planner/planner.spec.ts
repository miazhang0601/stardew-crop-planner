import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CropApi } from '../../crop-api';
import { CalculationResult, Crop, Plan } from '../../models';
import { Planner } from './planner';

const PARSNIP: Crop = {
  id: 1, name: 'Parsnip', seedPrice: 20, sellPrice: 35, growthDays: 4, regrowDays: null, harvestYield: 1, seasons: ['SPRING'],
};

const RANKING: CalculationResult[] = [
  {
    cropId: 1, cropName: 'Parsnip', season: 'SPRING', startDay: 1, tiles: 1, growingDays: 27,
    harvests: 6, seedsPerTile: 6, totalCost: 120, totalRevenue: 210, profit: 90, profitPerDay: 3.33,
  },
];

describe('Planner', () => {
  let api: jasmine.SpyObj<CropApi>;
  let router: jasmine.SpyObj<Router>;
  let fixture: ComponentFixture<Planner>;
  let component: Planner;

  beforeEach(async () => {
    api = jasmine.createSpyObj<CropApi>('CropApi', ['getCrops', 'getRanking', 'createPlan']);
    api.getCrops.and.returnValue(of([PARSNIP]));
    api.getRanking.and.returnValue(of(RANKING));
    api.createPlan.and.returnValue(of({} as Plan));
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [Planner],
      providers: [
        { provide: CropApi, useValue: api },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(Planner);
    component = fixture.componentInstance;
  });

  it('loads the crops and the ranking for the default season', fakeAsync(() => {
    fixture.detectChanges();
    tick(250);
    fixture.detectChanges();

    expect(api.getCrops).toHaveBeenCalledWith('SPRING');
    expect(api.getRanking).toHaveBeenCalledWith('SPRING', 1, 1);
    expect(fixture.nativeElement.querySelector('tbody th')?.textContent).toContain('Parsnip');
  }));

  it('is invalid until it has a name and at least one crop', fakeAsync(() => {
    fixture.detectChanges();
    tick(250);
    expect(component.form.valid).toBeFalse();

    component.form.controls.name.setValue('Year 1 spring');
    expect(component.form.valid).toBeFalse();

    component.addEntry(1);
    expect(component.form.valid).toBeTrue();
  }));

  it('rejects a planting day outside 1 to 28 and does not ask for a ranking', fakeAsync(() => {
    fixture.detectChanges();
    tick(250);
    api.getRanking.calls.reset();

    component.form.controls.startDay.setValue(40);
    tick(250);

    expect(component.form.controls.startDay.invalid).toBeTrue();
    expect(api.getRanking).not.toHaveBeenCalled();
  }));

  it('clears the chosen crops when the season changes', fakeAsync(() => {
    fixture.detectChanges();
    tick(250);
    component.addEntry(1);

    component.form.controls.season.setValue('FALL');
    tick(250);

    expect(component.entries.length).toBe(0);
    expect(api.getCrops).toHaveBeenCalledWith('FALL');
  }));

  it('does not call the API when the form is invalid', fakeAsync(() => {
    fixture.detectChanges();
    tick(250);

    component.save();

    expect(api.createPlan).not.toHaveBeenCalled();
  }));

  it('saves a valid plan and goes to the saved plans page', fakeAsync(() => {
    fixture.detectChanges();
    tick(250);
    component.form.controls.name.setValue('  Year 1 spring ');
    component.addEntry(1);

    component.save();

    expect(api.createPlan).toHaveBeenCalledWith({
      name: 'Year 1 spring',
      season: 'SPRING',
      startDay: 1,
      entries: [{ cropId: 1, tiles: 10 }],
    });
    expect(router.navigate).toHaveBeenCalledWith(['/plans']);
  }));

  it('shows the message from the API when saving fails', fakeAsync(() => {
    api.createPlan.and.returnValue(
      throwError(() => ({ error: { status: 400, message: 'Validation failed', fieldErrors: { name: 'Give the plan a name' } } })),
    );
    fixture.detectChanges();
    tick(250);
    component.form.controls.name.setValue('x');
    component.addEntry(1);

    component.save();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.error')?.textContent).toContain('Give the plan a name');
  }));
});
