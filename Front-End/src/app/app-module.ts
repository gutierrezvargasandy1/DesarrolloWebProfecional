import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { AuthModule } from './auth/auth-module';
import { HttpClientModule } from '@angular/common/http';
import { ReportesModule } from './reportes/reportes-module';
import { MisReportesModule } from './mis-reportes/mis-reportes-module';

@NgModule({
  declarations: [
    App
    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    AuthModule,
    ReportesModule,
    MisReportesModule,
    HttpClientModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
