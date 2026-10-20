import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommerceStrategy, StrategyRequest, StrategyResponse } from '../../models';

@Injectable({ providedIn: 'root' })
export class StrategyService {
  private readonly http = inject(HttpClient);
  readonly baseUrl = 'http://localhost:8080';

  getStrategy(): Observable<StrategyResponse> {
    return this.http.get<StrategyResponse>(`${this.baseUrl}/config/strategy`);
  }

  setStrategy(strategy: CommerceStrategy | string): Observable<StrategyResponse> {
    const body: StrategyRequest = { strategy };
    return this.http.put<StrategyResponse>(`${this.baseUrl}/config/strategy`, body);
  }
}
