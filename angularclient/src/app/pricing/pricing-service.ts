import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PricingService {
  
  private pricingUrl: string;

  constructor(private http: HttpClient) {
    this.pricingUrl = 'http://localhost:8080/pricing';
  }

  //public = tout le monde a le droit / private = uniquement ce fichier 
//findpricing = nom de la méthode et () donne les valeurs
  public findPricing(): Observable<string> 
  {
    return this.http.get<string>(this.pricingUrl);
  }

}
