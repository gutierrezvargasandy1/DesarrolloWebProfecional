import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

// Interceptors
import { AuthInterceptor } from './services/api/interceptors/auth.interceptor';
import { ErrorInterceptor } from './services/api/interceptors/error.interceptor';
import { LoadingInterceptor, LoadingService } from './services/api/interceptors/loading.interceptor';
import { LoggingInterceptor } from './services/api/interceptors/logging.interceptor';

// Services
import { ApiService } from './services/api/api.service';
import { AuthService } from './services/auth/auth.service';
import { TokenService } from './utils/token.service';
import { StorageService } from './services/storage/storage.service';
import { SessionStorageService } from './services/storage/session-storage.service';

// Guards
import { AuthGuard } from './guards/auth.guard';

@NgModule({
  imports: [CommonModule],
  providers: [
    // Servicios
    ApiService,
    AuthService,
    TokenService,
    StorageService,
    SessionStorageService,
    LoadingService,
    
    // Guards
    AuthGuard,
    
    // Interceptores (orden importante)
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoggingInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoadingInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule ya está cargado. Impórtalo solo en AppModule');
    }
  }
}