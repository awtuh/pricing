import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PricingService } from './pricing/pricing-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('angularclient');
  
  // ✅ Déclarez toutes les propriétés nécessaires
  public pricingData: string = '';
  public isLoading: boolean = false;
  public error: string = '';

  constructor(private pricingService: PricingService) {
    this.loadPricing();
  }

  public loadPricing(): void {
    this.isLoading = true;
    this.error = '';
    
    this.pricingService.findPricing().subscribe({
      next: (data: string) => {
        this.pricingData = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement: ' + err.message;
        this.isLoading = false;
        console.error('Erreur:', err);
      }
    });
  }

  public refreshPricing(): void {
    this.loadPricing();
  }
}