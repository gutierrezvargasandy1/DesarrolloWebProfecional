import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

// Servicio global de loading (crear si no existe)
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private loading = false;
  private listeners: ((loading: boolean) => void)[] = [];

  show() { this.loading = true; this.notify(); }
  hide() { this.loading = false; this.notify(); }
  isLoading() { return this.loading; }
  onLoadingChange(callback: (loading: boolean) => void) { this.listeners.push(callback); }
  private notify() { this.listeners.forEach(fn => fn(this.loading)); }
}

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private totalRequests = 0;

  constructor(private loadingService: LoadingService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Verificar si debe mostrar loader (por header o por defecto true)
    const showLoader = req.headers.get('X-Show-Loader') !== 'false';
    
    if (showLoader) {
      this.totalRequests++;
      this.loadingService.show();
    }

    return next.handle(req).pipe(
      finalize(() => {
        if (showLoader) {
          this.totalRequests--;
          if (this.totalRequests === 0) {
            this.loadingService.hide();
          }
        }
      })
    );
  }
}