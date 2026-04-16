import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';

import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';

import { TokenService } from '../../../utils/token.service';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private tokenService: TokenService,
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    console.log('🔥 INTERCEPTOR ACTIVO:', req.url);

    // Skip auth
    if (req.headers.has('X-Skip-Auth')) {
      return next.handle(req.clone({ headers: req.headers.delete('X-Skip-Auth') }));
    }

    // Public endpoints
    const publicEndpoints = [
      '/auth/login',
      '/auth/register',
      '/auth/forgot-password',
      '/auth/verify-code',
      '/auth/change-password'
    ];

    if (publicEndpoints.some(e => req.url.includes(e))) {
      return next.handle(req);
    }

    const token = this.tokenService.getAccessToken();

    if (token) {
      req = this.addToken(req, token);
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {

        const backendMessage =
          error?.error?.message ||
          error?.error ||
          error?.message;

        console.log('❌ HTTP ERROR DETECTADO:', error.status, backendMessage);

        if (error.status === 401) {

          // 🔥 ACEPTA CUALQUIER VARIANTE
          if (
            backendMessage?.toLowerCase().includes('expir') ||
            backendMessage?.toLowerCase().includes('token')
          ) {
            return this.handle401Error(req, next);
          }

          // token inválido
          this.tokenService.clearToken();
          this.router.navigate(['/auth/login']);
        }

        return throwError(() => error);
      })
    );
  }

  private addToken(req: HttpRequest<any>, token: string) {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler) {

    if (!this.isRefreshing) {

      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.authService.refreshToken().pipe(
        switchMap((res: any) => {

          this.isRefreshing = false;

          const newToken = res.accessToken;

          this.tokenService.setAccessToken(newToken);
          this.refreshTokenSubject.next(newToken);

          return next.handle(this.addToken(req, newToken));
        }),
        catchError(err => {
          this.isRefreshing = false;
          this.tokenService.clearToken();
          this.router.navigate(['/auth/login']);
          return throwError(() => err);
        })
      );
    }

    return this.refreshTokenSubject.pipe(
      filter(t => t !== null),
      take(1),
      switchMap(token =>
        next.handle(this.addToken(req, token!))
      )
    );
  }
}