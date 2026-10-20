import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import {
  PricingSuggestion,
  ReorderSuggestion,
  SuggestionStatus,
  SuggestionStatusUpdateRequest
} from '../../models';

@Injectable({ providedIn: 'root' })
export class SuggestionService {
  private readonly http = inject(HttpClient);
  readonly baseUrl = 'http://localhost:8080';

  /**
   * Fetches pricing suggestions.
   * If status is empty or 'ALL', queries PENDING, ACCEPTED, and REJECTED in parallel
   * because the Spring Boot backend defaults to PENDING when status is omitted.
   */
  getPricingSuggestions(status?: SuggestionStatus | '' | 'ALL'): Observable<PricingSuggestion[]> {
    if (!status || status === 'ALL') {
      return forkJoin([
        this.fetchPricingByStatus('PENDING'),
        this.fetchPricingByStatus('ACCEPTED'),
        this.fetchPricingByStatus('REJECTED')
      ]).pipe(
        map(([pending, accepted, rejected]) => {
          const combined = [...pending, ...accepted, ...rejected];
          const mapById = new Map<number, PricingSuggestion>();
          for (const item of combined) {
            mapById.set(item.id, item);
          }
          return Array.from(mapById.values()).sort((a, b) => b.id - a.id);
        })
      );
    }

    return this.fetchPricingByStatus(status);
  }

  private fetchPricingByStatus(status: SuggestionStatus): Observable<PricingSuggestion[]> {
    const params = new HttpParams().set('status', status);
    return this.http.get<PricingSuggestion[]>(`${this.baseUrl}/suggestions/pricing`, { params });
  }

  getPricingSuggestionById(id: number): Observable<PricingSuggestion> {
    return this.http.get<PricingSuggestion>(`${this.baseUrl}/suggestions/pricing/${id}`);
  }

  updatePricingSuggestionStatus(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<PricingSuggestion> {
    const body: SuggestionStatusUpdateRequest = { status };
    return this.http.patch<PricingSuggestion>(`${this.baseUrl}/suggestions/pricing/${id}`, body);
  }

  /**
   * Fetches reorder suggestions.
   * If status is empty or 'ALL', queries PENDING, ACCEPTED, and REJECTED in parallel
   * because the Spring Boot backend defaults to PENDING when status is omitted.
   */
  getReorderSuggestions(status?: SuggestionStatus | '' | 'ALL'): Observable<ReorderSuggestion[]> {
    if (!status || status === 'ALL') {
      return forkJoin([
        this.fetchReorderByStatus('PENDING'),
        this.fetchReorderByStatus('ACCEPTED'),
        this.fetchReorderByStatus('REJECTED')
      ]).pipe(
        map(([pending, accepted, rejected]) => {
          const combined = [...pending, ...accepted, ...rejected];
          const mapById = new Map<number, ReorderSuggestion>();
          for (const item of combined) {
            mapById.set(item.id, item);
          }
          return Array.from(mapById.values()).sort((a, b) => b.id - a.id);
        })
      );
    }

    return this.fetchReorderByStatus(status);
  }

  private fetchReorderByStatus(status: SuggestionStatus): Observable<ReorderSuggestion[]> {
    const params = new HttpParams().set('status', status);
    return this.http.get<ReorderSuggestion[]>(`${this.baseUrl}/suggestions/reorder`, { params });
  }

  getReorderSuggestionById(id: number): Observable<ReorderSuggestion> {
    return this.http.get<ReorderSuggestion>(`${this.baseUrl}/suggestions/reorder/${id}`);
  }

  updateReorderSuggestionStatus(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<ReorderSuggestion> {
    const body: SuggestionStatusUpdateRequest = { status };
    return this.http.patch<ReorderSuggestion>(`${this.baseUrl}/suggestions/reorder/${id}`, body);
  }
}
