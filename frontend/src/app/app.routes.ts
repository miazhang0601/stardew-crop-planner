import { Routes } from '@angular/router';
import { CropList } from './pages/crop-list/crop-list';
import { Planner } from './pages/planner/planner';
import { Plans } from './pages/plans/plans';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'crops' },
  { path: 'crops', component: CropList, title: 'Crops' },
  { path: 'planner', component: Planner, title: 'Planner' },
  { path: 'plans', component: Plans, title: 'Saved plans' },
  { path: '**', redirectTo: 'crops' },
];
