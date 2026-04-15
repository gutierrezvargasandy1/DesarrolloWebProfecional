import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';

@Injectable()
export class LoggingInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (environment.production) {
      return next.handle(req);
    }

    const startTime = Date.now();
    const method = req.method;
    const url = req.url;

    console.log(`[${method}] ${url}`, {
      body: req.body,
      headers: req.headers.keys()
    });

    return next.handle(req).pipe(
      tap({
        next: (event) => {
          if (event instanceof HttpResponse) {
            const duration = Date.now() - startTime;
            console.log(`[${method}] ${url} - ${event.status} (${duration}ms)`, {
              body: event.body,
              status: event.status
            });
          }
        },
        error: (error) => {
          const duration = Date.now() - startTime;
          console.error(`[${method}] ${url} - ${error.status} (${duration}ms)`, error);
        }
      })
    );
  }
}