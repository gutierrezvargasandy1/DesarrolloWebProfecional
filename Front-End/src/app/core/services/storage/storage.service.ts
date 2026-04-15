import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private storage = localStorage;

  set(key: string, value: any): void {
    try {
      const serialized = JSON.stringify(value);
      this.storage.setItem(key, serialized);
    } catch (error) {
      console.error(`Error saving to localStorage: ${key}`, error);
    }
  }

  get<T>(key: string): T | null {
    try {
      const serialized = this.storage.getItem(key);
      if (serialized === null) return null;
      return JSON.parse(serialized) as T;
    } catch (error) {
      console.error(`Error reading from localStorage: ${key}`, error);
      return null;
    }
  }

  remove(key: string): void {
    this.storage.removeItem(key);
  }

  clear(): void {
    this.storage.clear();
  }

  has(key: string): boolean {
    return this.storage.getItem(key) !== null;
  }
}