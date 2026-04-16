import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface Coordenadas {
  lat: number;
  lon: number;
  displayName: string;
}

interface NominatimResult {
  lat: string;
  lon: string;
  display_name: string;
}

@Injectable({ providedIn: 'root' })
export class GeocodingService {
  private readonly NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';

  constructor(private http: HttpClient) {}

  buscarCoordenadas(direccion: string): Observable<Coordenadas | null> {
    const query = direccion?.trim();
    if (!query || query.length < 3) {
      return of(null);
    }

    const params = new HttpParams()
      .set('q', query)
      .set('format', 'json')
      .set('limit', '1')
      .set('addressdetails', '0');

    return this.http.get<NominatimResult[]>(this.NOMINATIM_URL, { params }).pipe(
      map(results => {
        if (!results || results.length === 0) return null;
        const r = results[0];
        return {
          lat: parseFloat(r.lat),
          lon: parseFloat(r.lon),
          displayName: r.display_name,
        } as Coordenadas;
      }),
      catchError(() => of(null))
    );
  }
}