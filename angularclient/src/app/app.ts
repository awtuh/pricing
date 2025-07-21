import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface Instrument {
  symbol: string;
  name: string;
  price: number;
  previousPrice: number;
  change: number;
  changePercent: number;
  category: string;
  hasSignal: boolean;
  signalType?: 'BUY' | 'SELL';
  signalStrength?: number;
  trend: 'UP' | 'DOWN' | 'NEUTRAL';
  volume?: number;
  lastUpdate: Date;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class AppComponent implements OnInit {
  title = 'Trading Dashboard Pro';
  
  // 🎯 PROPRIÉTÉS PRINCIPALES
  instruments: Instrument[] = [];
  isLoading = true;
  error = '';
  activeSignals = 0;
  totalInstruments = 0;
  lastUpdate: Date = new Date();

  // 🎯 URL CORRIGÉE DE VOTRE API JAVA
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadAllInstruments();
    
    // 🎯 RAFRAÎCHISSEMENT AUTO TOUTES LES 30 SECONDES (pour éviter rate limiting)
    setInterval(() => {
      this.loadAllInstruments();
    }, 30000);
  }

  // 🎯 CHARGER TOUTES LES DONNÉES DEPUIS VOTRE API JAVA
  loadAllInstruments() {
    this.isLoading = true;
    this.error = '';
    
    console.log('🚀 Chargement depuis:', `${this.apiUrl}/instruments`);

    // 🎯 APPEL VERS VOTRE VRAIE API JAVA
    this.http.get<any>(`${this.apiUrl}/instruments`).subscribe({
      next: (response: any) => {
        console.log('📡 Réponse complète de l\'API:', response);
        
        if (response && response.success && response.data) {
          this.processApiData(response.data);
        } else {
          console.warn('⚠️ Format de réponse inattendu:', response);
          this.processApiData(response); // Fallback au cas où
        }
      },
      error: (httpError: any) => {
        console.error('❌ Erreur API:', httpError);
        this.error = 'Impossible de charger les données depuis l\'API Java';
        this.isLoading = false;
        
        // 🎯 DONNÉES DE FALLBACK EN CAS D'ERREUR
        this.loadFallbackData();
      }
    });
  }

  // 🎯 TRAITEMENT DES DONNÉES DE L'API JAVA
  processApiData(apiData: any) {
    this.instruments = [];
    
    try {
      console.log('🔄 Traitement des données API:', apiData);

      // 🎯 TRAITEMENT DES STOCKS
      if (apiData.stocks) {
        Object.keys(apiData.stocks).forEach(symbol => {
          const stockData = apiData.stocks[symbol];
          if (!stockData.error) { // Ignore les erreurs
            this.instruments.push(this.createInstrument(symbol, stockData, 'STOCKS'));
          }
        });
      }

      // 🎯 TRAITEMENT DES COMMODITIES  
      if (apiData.commodities) {
        Object.keys(apiData.commodities).forEach(symbol => {
          const commodityData = apiData.commodities[symbol];
          if (!commodityData.error) { // Ignore les erreurs
            this.instruments.push(this.createInstrument(symbol, commodityData, 'COMMODITIES'));
          }
        });
      }

      // 🎯 TRAITEMENT DU FOREX
      if (apiData.forex) {
        Object.keys(apiData.forex).forEach(symbol => {
          const forexData = apiData.forex[symbol];
          if (!forexData.error) { // Ignore les erreurs
            this.instruments.push(this.createInstrument(symbol, forexData, 'FOREX'));
          }
        });
      }

      // 🎯 TRI ET FINALISATION
      this.sortInstruments();
      this.calculateSignals();
      this.activeSignals = this.instruments.filter(i => i.hasSignal).length;
      this.totalInstruments = this.instruments.length;
      this.isLoading = false;
      this.lastUpdate = new Date();
      this.error = '';

      console.log(`✅ ${this.totalInstruments} instruments chargés avec succès`);
      
    } catch (error) {
      console.error('❌ Erreur lors du traitement des données:', error);
      this.error = 'Erreur lors du traitement des données';
      this.loadFallbackData();
    }
  }

  // 🎯 CRÉER UN OBJET INSTRUMENT À PARTIR DES DONNÉES API
  createInstrument(symbol: string, data: any, category: string): Instrument {
    const price = parseFloat(data.price) || 0;
    const previousPrice = price * (0.98 + Math.random() * 0.04); // Simulation prix précédent
    const change = price - previousPrice;
    const changePercent = previousPrice !== 0 ? (change / previousPrice) * 100 : 0;

    return {
      symbol: symbol,
      name: this.getInstrumentName(symbol),
      price: price,
      previousPrice: previousPrice,
      change: change,
      changePercent: changePercent,
      category: category,
      hasSignal: false, // Calculé plus tard
      signalType: undefined,
      signalStrength: undefined,
      trend: change > 0 ? 'UP' : change < 0 ? 'DOWN' : 'NEUTRAL',
      volume: Math.floor(Math.random() * 1000000),
      lastUpdate: new Date(data.timestamp || Date.now())
    };
  }

  // 🎯 CALCULER LES SIGNAUX DE TRADING
  calculateSignals() {
    this.instruments.forEach(instrument => {
      // Logique simple de signaux basée sur les variations
      const shouldHaveSignal = Math.abs(instrument.changePercent) > 1; // Si variation > 1%
      
      if (shouldHaveSignal) {
        instrument.hasSignal = true;
        instrument.signalType = instrument.changePercent > 0 ? 'BUY' : 'SELL';
        instrument.signalStrength = Math.min(5, Math.floor(Math.abs(instrument.changePercent)) + 1);
      } else {
        instrument.hasSignal = Math.random() > 0.7; // 30% de chance d'avoir un signal
        if (instrument.hasSignal) {
          instrument.signalType = Math.random() > 0.5 ? 'BUY' : 'SELL';
          instrument.signalStrength = Math.floor(Math.random() * 5) + 1;
        }
      }
    });
  }

  // 🎯 OBTENIR LE NOM COMPLET D'UN INSTRUMENT
  getInstrumentName(symbol: string): string {
    const names: { [key: string]: string } = {
      'AAPL': 'Apple Inc.',
      'GOOGL': 'Alphabet Inc.',
      'MSFT': 'Microsoft Corporation',
      'TSLA': 'Tesla Inc.',
      'NVDA': 'NVIDIA Corporation',
      'GOLD': 'Gold Spot',
      'SILVER': 'Silver Spot',
      'OIL': 'Crude Oil',
      'NATGAS': 'Natural Gas',
      'COPPER': 'Copper',
      'EURUSD': 'Euro/US Dollar',
      'GBPUSD': 'British Pound/US Dollar',
      'USDJPY': 'US Dollar/Japanese Yen',
      'USDCHF': 'US Dollar/Swiss Franc',
      'AUDUSD': 'Australian Dollar/US Dollar'
    };
    return names[symbol] || symbol;
  }

  // 🎯 TRIER LES INSTRUMENTS PAR CATÉGORIE
  sortInstruments() {
    this.instruments.sort((a, b) => {
      if (a.category !== b.category) {
        const getCategoryPriority = (category: string): number => {
          switch(category) {
            case 'STOCKS': return 1;
            case 'COMMODITIES': return 2;
            case 'FOREX': return 3;
            default: return 99;
          }
        };
        
        return getCategoryPriority(a.category) - getCategoryPriority(b.category);
      }
      return a.symbol.localeCompare(b.symbol);
    });
  }

  // 🎯 DONNÉES DE SECOURS SI L'API NE RÉPOND PAS
  loadFallbackData() {
    this.instruments = [
      {
        symbol: 'AAPL',
        name: 'Apple Inc.',
        price: 175.50,
        previousPrice: 174.20,
        change: 1.30,
        changePercent: 0.75,
        category: 'STOCKS',
        hasSignal: true,
        signalType: 'BUY',
        signalStrength: 4,
        trend: 'UP',
        volume: 45000000,
        lastUpdate: new Date()
      },
      {
        symbol: 'GOLD',
        name: 'Gold Spot',
        price: 2001.25,
        previousPrice: 1998.80,
        change: 2.45,
        changePercent: 0.12,
        category: 'COMMODITIES',
        hasSignal: true,
        signalType: 'BUY',
        signalStrength: 3,
        trend: 'UP',
        volume: 125000,
        lastUpdate: new Date()
      },
      {
        symbol: 'EURUSD',
        name: 'Euro/US Dollar',
        price: 1.0845,
        previousPrice: 1.0820,
        change: 0.0025,
        changePercent: 0.23,
        category: 'FOREX',
        hasSignal: false,
        trend: 'UP',
        volume: 2500000,
        lastUpdate: new Date()
      }
    ];

    this.sortInstruments();
    this.activeSignals = this.instruments.filter(i => i.hasSignal).length;
    this.totalInstruments = this.instruments.length;
    this.isLoading = false;
    this.lastUpdate = new Date();
  }

  // 🎯 METHODS UTILITAIRES POUR LE TEMPLATE
  getInstrumentsByCategory(category: string): Instrument[] {
    return this.instruments.filter(i => i.category === category);
  }

  getInstrumentsWithSignals(): Instrument[] {
    return this.instruments.filter(i => i.hasSignal);
  }

  refreshData() {
    this.loadAllInstruments();
  }

  getCategories(): string[] {
    return ['STOCKS', 'COMMODITIES', 'FOREX'];
  }
}
