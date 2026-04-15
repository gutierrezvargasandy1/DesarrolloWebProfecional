import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders, HttpContext } from '@angular/common/http';
import { Observable, throwError, TimeoutError } from 'rxjs';
import { catchError, timeout, retry, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ApiOptions {
  headers?: HttpHeaders;
  params?: HttpParams | { [param: string]: string | number | boolean | readonly (string | number | boolean)[] };
  context?: HttpContext;
  showLoader?: boolean;
  timeout?: number;
  retryCount?: number;
  skipAuth?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = environment.apiUrl || 'http://localhost:5000/api';
  private defaultTimeout = 30000;
  private defaultRetry = 1;

  constructor(private http: HttpClient) {}

  // GET
  get<T>(endpoint: string, options?: ApiOptions): Observable<T> {
    return this.request<T>('GET', endpoint, null, options);
  }

  // POST
  post<T>(endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    return this.request<T>('POST', endpoint, body, options);
  }

  // PUT
  put<T>(endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    return this.request<T>('PUT', endpoint, body, options);
  }

  // PATCH
  patch<T>(endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    return this.request<T>('PATCH', endpoint, body, options);
  }

  // DELETE
  delete<T>(endpoint: string, options?: ApiOptions): Observable<T> {
    return this.request<T>('DELETE', endpoint, null, options);
  }

  // Subir archivo
  upload<T>(endpoint: string, file: File, additionalData?: any, options?: ApiOptions): Observable<T> {
    const formData = new FormData();
    formData.append('file', file);
    
    if (additionalData) {
      Object.keys(additionalData).forEach(key => {
        formData.append(key, additionalData[key]);
      });
    }

    const uploadOptions = {
      ...options,
      headers: options?.headers?.set('Content-Type', 'multipart/form-data')
    };

    return this.post<T>(endpoint, formData, uploadOptions);
  }

  // Descargar archivo
  download(endpoint: string, options?: ApiOptions): Observable<Blob> {
    return this.http.get(`${this.baseUrl}${endpoint}`, {
      ...this.buildOptions(options),
      responseType: 'blob'
    }).pipe(
      timeout(options?.timeout || this.defaultTimeout),
      retry(options?.retryCount || this.defaultRetry),
      catchError(this.handleError)
    );
  }

  // Método principal que maneja todas las peticiones
  private request<T>(method: string, endpoint: string, body?: any, options?: ApiOptions): Observable<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const httpOptions = this.buildOptions(options);

    // Agregar header para indicar si debe mostrar loader
    const finalOptions = {
      ...httpOptions,
      headers: httpOptions.headers?.set('X-Show-Loader', options?.showLoader !== false ? 'true' : 'false')
    };

    let request$: Observable<T>;

    switch (method) {
      case 'GET':
        request$ = this.http.get<T>(url, finalOptions);
        break;
      case 'POST':
        request$ = this.http.post<T>(url, body, finalOptions);
        break;
      case 'PUT':
        request$ = this.http.put<T>(url, body, finalOptions);
        break;
      case 'PATCH':
        request$ = this.http.patch<T>(url, body, finalOptions);
        break;
      case 'DELETE':
        request$ = this.http.delete<T>(url, finalOptions);
        break;
      default:
        throw new Error(`Método HTTP no soportado: ${method}`);
    }

    return request$.pipe(
      timeout(options?.timeout || this.defaultTimeout),
      retry(options?.retryCount || this.defaultRetry),
      tap(response => this.logResponse(method, url, response)),
      catchError(this.handleError)
    );
  }

  private buildOptions(options?: ApiOptions) {
    let headers = options?.headers || new HttpHeaders();
    
    // Headers por defecto
    headers = headers.set('Content-Type', 'application/json');
    headers = headers.set('Accept', 'application/json');

    // Si skipAuth está en true, agregar header especial
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

  private logResponse(method: string, url: string, response: any): void {
    if (!environment.production) {
      console.log(`${method} ${url} ->`, response);
    }
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'Error desconocido';

    if (error instanceof TimeoutError) {
      errorMessage = 'La petición ha tardado demasiado tiempo';
    } else if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.status) {
      errorMessage = `Error ${error.status}: ${error.message}`;
    }

    console.error('API Error:', errorMessage, error);
    return throwError(() => ({ message: errorMessage, originalError: error }));
  }
}