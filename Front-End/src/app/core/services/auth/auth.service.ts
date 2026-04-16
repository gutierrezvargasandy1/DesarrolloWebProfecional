import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { ApiService } from '../api/api.service';
import { TokenService } from '../../utils/token.service';
import { API_ENDPOINTS } from '../api/endpoints';
import { ApiResponse } from '../api/model/api.responce';
import { LoginData } from './model/login-data';


@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(
    private api: ApiService,
    private tokenService: TokenService
  ) {}

  login(credentials: { email: string; password: string }) {
    return this.api
      .post<ApiResponse<LoginData>>(API_ENDPOINTS.AUTH.LOGIN, credentials)
      .pipe(
        tap(res => {
          const { accessToken } = res.data;
          this.tokenService.setAccessToken(accessToken);
        })
      );
  }

  refreshToken() {
    return this.api
      .post<ApiResponse<{ accessToken: string }>>(
        API_ENDPOINTS.AUTH.REFRESH_TOKEN,
        {}
      )
      .pipe(
        tap(res => {
          this.tokenService.setAccessToken(res.data.accessToken);
        })
      );
  }



  forgotPassword(email: string) {
    return this.api.post<ApiResponse<boolean>>(
      API_ENDPOINTS.AUTH.FORGOT_PASSWORD,
      { correo: email }
    );
  }

  verifyCode(correo: string, codigo: string) {
    return this.api.post<ApiResponse<boolean>>(
      API_ENDPOINTS.AUTH.VERIFY_CODE,
      { correo, codigo }
    );
  }

  changePassword(correo: string, codigo: string, nueva_password: string) {
    return this.api.post<ApiResponse<boolean>>(
      API_ENDPOINTS.AUTH.CHANGE_PASSWORD,
      { correo, codigo, nueva_password }
    );
  }

  


}