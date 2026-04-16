import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
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
    ) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Saltar autenticación si el header lo indica
        if (req.headers.has('X-Skip-Auth')) {
            const newReq = req.clone({ headers: req.headers.delete('X-Skip-Auth') });
            return next.handle(newReq);
        }

        // Saltar endpoints públicos
        const publicEndpoints = [  '/auth/login','/auth/register', '/auth/forgot-password','/auth/verify-code','/auth/change-password'];
        if (publicEndpoints.some(endpoint => req.url.includes(endpoint))) {
            return next.handle(req);
        }

        const token = this.tokenService.getAccessToken();

        if (token) {
            req = this.addToken(req, token);
        }

        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {

                if (error.status === 401) {

                    const message = error.error?.message;

                    if (message === 'Token expirado') {
                        return this.handle401Error(req, next);
                    }

                    if (message === 'Token inválido') {
                        this.tokenService.clearToken();
                        this.router.navigate(['auth/login']);
                        return throwError(() => error);
                    }
                }

                return throwError(() => error);
            })
        );
    }

    private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
        return request.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }

    private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);

            return this.authService.refreshToken().pipe(
                switchMap((response: any) => {
                    this.isRefreshing = false;
                    this.tokenService.setAccessToken(response.accessToken);
                    this.refreshTokenSubject.next(response.accessToken);
                    return next.handle(this.addToken(request, response.accessToken));
                }),
                catchError(error => {
                    this.isRefreshing = false;
                    this.tokenService.clearToken();
                    return throwError(() => error);
                })
            );
        } else {
            return this.refreshTokenSubject.pipe(
                filter(token => token !== null),
                take(1),
                switchMap(token => next.handle(this.addToken(request, token!)))
            );
        }
    }
}