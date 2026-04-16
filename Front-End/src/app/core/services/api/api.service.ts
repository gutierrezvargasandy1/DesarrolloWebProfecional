import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams,
  HttpHeaders,
  HttpContext
} from '@angular/common/http';

import { Observable, throwError, TimeoutError } from 'rxjs';
import { catchError, timeout, retry, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ApiOptions {
  headers?: HttpHeaders;
  params?: HttpParams | { [param: string]: any };
  context?: HttpContext;
  showLoader?: boolean;
  timeout?: number;
  retryCount?: number;
  skipAuth?: boolean;
  responseType?: 'json' | 'blob';
}

@Injectable({ providedIn: 'root' })
export class ApiService {

  private baseUrl = environment.apiUrl || 'http://localhost:5000/api';
  private defaultTimeout = 30000;
  private defaultRetry = 1;

  constructor(private http: HttpClient) {}

  // ================= GET =================
  get<T>(endpoint: string, options?: ApiOptions): Observable<T> {
    return this.request<T>('GET', endpoint, null, options);
  }

  // ================= POST =================
  post<T>(endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    return this.request<T>('POST', endpoint, body, options);
  }

  // ================= PUT =================
  put<T>(endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    return this.request<T>('PUT', endpoint, body, options);
  }

  // ================= PATCH =================
  patch<T>(endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    return this.request<T>('PATCH', endpoint, body, options);
  }

  // ================= DELETE =================
  delete<T>(endpoint: string, options?: ApiOptions): Observable<T> {
    return this.request<T>('DELETE', endpoint, null, options);
  }

  // ================= CORE REQUEST (FIX DEFINITIVO) =================
  private request<T>(
    method: string,
    endpoint: string,
    body: any,
    options?: ApiOptions
  ): Observable<T> {

    const url = `${this.baseUrl}${endpoint}`;

    const httpOptions = this.buildOptions(body, options);

    const headers = httpOptions.headers?.set(
      'X-Show-Loader',
      options?.showLoader === false ? 'false' : 'true'
    );

    const baseOptions = {
      ...httpOptions,
      headers
    };

    switch (method) {

      case 'GET':
        return this.http.get<T>(url, baseOptions).pipe(
          timeout(options?.timeout || this.defaultTimeout),
          retry(options?.retryCount || this.defaultRetry),
          tap(res => this.log(method, url, res)),
          catchError(this.handleError)
        );

      case 'POST':
        return this.http.post<T>(url, body, baseOptions).pipe(
          timeout(options?.timeout || this.defaultTimeout),
          retry(options?.retryCount || this.defaultRetry),
          tap(res => this.log(method, url, res)),
          catchError(this.handleError)
        );

      case 'PUT':
        return this.http.put<T>(url, body, baseOptions).pipe(
          timeout(options?.timeout || this.defaultTimeout),
          retry(options?.retryCount || this.defaultRetry),
          tap(res => this.log(method, url, res)),
          catchError(this.handleError)
        );

      case 'PATCH':
        return this.http.patch<T>(url, body, baseOptions).pipe(
          timeout(options?.timeout || this.defaultTimeout),
          retry(options?.retryCount || this.defaultRetry),
          tap(res => this.log(method, url, res)),
          catchError(this.handleError)
        );

      case 'DELETE':
        return this.http.delete<T>(url, baseOptions).pipe(
          timeout(options?.timeout || this.defaultTimeout),
          retry(options?.retryCount || this.defaultRetry),
          tap(res => this.log(method, url, res)),
          catchError(this.handleError)
        );

      default:
        return throwError(() => new Error('Método no soportado'));
    }
  }

  // ================= HEADERS =================
  private buildOptions(body: any, options?: ApiOptions) {

    let headers = options?.headers || new HttpHeaders();

    const isFormData = body instanceof FormData;

    if (!isFormData) {
      headers = headers.set('Content-Type', 'application/json');
    }

    headers = headers.set('Accept', 'application/json');

    if (options?.skipAuth) {
      headers = headers.set('X-Skip-Auth', 'true');
    }

    return {
      headers,
      params: options?.params,
      context: options?.context,
      withCredentials: true
    };
  }

  // ================= LOG =================
  private log(method: string, url: string, res: any) {
    if (!environment.production) {
      console.log(`[API] ${method} ${url}`, res);
    }
  }

  // ================= ERROR =================
  private handleError(error: any): Observable<never> {

    let message = 'Error desconocido';

    if (error instanceof TimeoutError) {
      message = 'Timeout de petición';
    } else if (error?.status) {
      message = `Error ${error.status}: ${error?.error?.message || error.message}`;
    }

    console.error('API ERROR:', message, error);

    return throwError(() => ({
      message,
      originalError: error
    }));
  }
}