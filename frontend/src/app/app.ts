import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="site-header">
      <h1>🌾 Stardew Crop Planner</h1>
      <nav aria-label="Main">
        <a routerLink="/crops" routerLinkActive="active">Crops</a>
        <a routerLink="/planner" routerLinkActive="active">Planner</a>
        <a routerLink="/plans" routerLinkActive="active">Saved plans</a>
      </nav>
    </header>
    <main>
      <router-outlet />
    </main>
    <footer>Fan made project. Not affiliated with ConcernedApe or Stardew Valley.</footer>
  `,
})
export class App {}
