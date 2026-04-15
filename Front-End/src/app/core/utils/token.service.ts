import { Injectable } from '@angular/core';
import { SessionStorageService } from '../services/storage/session-storage.service';

@Injectable({ providedIn: 'root' })
export class TokenService {

  private readonly ACCESS_TOKEN_KEY = 'access_token';

  constructor(private session: SessionStorageService) {}

  setAccessToken(token: string): void {
    this.session.set(this.ACCESS_TOKEN_KEY, token);
  }

  getAccessToken(): string | null {
    return this.session.get<string>(this.ACCESS_TOKEN_KEY);
  }

  clearToken(): void {
    this.session.remove(this.ACCESS_TOKEN_KEY);
  }


  hasValidAccessToken(): boolean {
  const token = this.getAccessToken();   
  if (!token) return false;             
  return !this.isTokenExpired();        
}

isTokenExpired(): boolean {
  const token = this.getAccessToken();
  if (!token) return true;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp; 
    if (!exp) return true;

    const now = Math.floor(Date.now() / 1000); 
    return now >= exp;
  } catch (e) {
   
    return true;
  }
}

  
}