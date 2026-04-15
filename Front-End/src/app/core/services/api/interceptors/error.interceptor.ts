import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

export interface ApiError {
  status: number;
  message: string;
  errors?: Record<string, string[]>;
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = this.parseError(error);
        this.handleErrorByStatus(apiError, error);
        return throwError(() => apiError);
      })
    );
  }

  private parseError(error: HttpErrorResponse): ApiError {
    // Error de red
    if (error.status === 0) {
      return {
        status: 0,
        message: 'No se pudo conectar con el servidor. Verifica tu conexión a internet.'
      };
    }

    if (error.error?.message) {
      return {
        status: error.status,
        message: error.error.message,
        errors: error.error.errors
      };
    }

    // Errores estándar
    const messages: Record<number, string> = {
      400: 'Solicitud incorrecta. Verifica los datos ingresados.',
      401: 'No autorizado. Por favor inicia sesión nuevamente.',
      403: 'No tienes permiso para realizar esta acción.',
      404: 'El recurso solicitado no existe.',
      409: 'Conflicto con el estado actual del recurso.',
      422: 'Error de validación. Verifica los campos.',
      429: 'Demasiadas solicitudes. Espera un momento.',
      500: 'Error interno del servidor. Intenta más tarde.',
      502: 'El servidor no está disponible temporalmente.',
      503: 'Servicio no disponible. Intenta más tarde.'
    };

    return {
      status: error.status,
      message: messages[error.status] || `Error ${error.status}: ${error.message}`
    };
  }

  private handleErrorByStatus(error: ApiError, originalError: HttpErrorResponse): void {
    // Log para debugging
    console.error('HTTP Error:', {
      status: error.status,
      message: error.message,
      url: originalError.url,
      timestamp: new Date().toISOString()
    });

    
  }
}