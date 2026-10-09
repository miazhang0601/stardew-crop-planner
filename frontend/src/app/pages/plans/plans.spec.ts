import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CropApi } from '../../crop-api';
import { Plan } from '../../models';
import { Plans } from './plans';

const PLAN: Plan = {
  id: 3, name: 'Berry summer', season: 'SUMMER', startDay: 1, createdAt: '2026-10-09T12:00:00Z',
  entries: [
    {
      cropId: 11, cropName: 'Blueberry', season: 'SUMMER', startDay: 1, tiles: 20, growingDays: 27,
      harvests: 4, seedsPerTile: 1, totalCost: 1600, totalRevenue: 12000, profit: 10400, profitPerDay: 385.19,
    },
  ],
  totalCost: 1600, totalRevenue: 12000, totalProfit: 10400,
};

describe('Plans', () => {
  let api: jasmine.SpyObj<CropApi>;
  let fixture: ComponentFixture<Plans>;

  beforeEach(async () => {
    api = jasmine.createSpyObj<CropApi>('CropApi', ['getPlans', 'deletePlan']);
    api.getPlans.and.returnValue(of([PLAN]));
    api.deletePlan.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [Plans],
      providers: [{ provide: CropApi, useValue: api }, provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(Plans);
    fixture.detectChanges();
  });

  it('shows each saved plan with its total profit', () => {
    const card = fixture.nativeElement.querySelector('.card') as HTMLElement;

    expect(card.querySelector('h3')?.textContent).toContain('Berry summer');
    expect(card.querySelector('tfoot')?.textContent).toContain('10,400g');
  });

  it('removes a plan from the page after deleting it', () => {
    (fixture.nativeElement.querySelector('.card button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(api.deletePlan).toHaveBeenCalledWith(3);
    expect(fixture.nativeElement.querySelector('.card')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('No plans saved yet');
  });
});
