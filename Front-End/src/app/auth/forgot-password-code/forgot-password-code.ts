import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';
import { SessionStorageService } from '../../core/services/storage/session-storage.service';

@Component({
  selector: 'app-forgot-password-code',
  standalone: false,
  templateUrl: './forgot-password-code.html',
  styleUrls: ['./forgot-password-code.css']
})
export class ForgotPasswordCode {
  form: FormGroup;
  correo: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private sessionStorage: SessionStorageService
  ) {
    const navigation = this.router.getCurrentNavigation();
    this.correo = navigation?.extras?.state?.['correo'] || this.sessionStorage.get<string>('resetCorreo') || '';
    
    if (!this.correo) {
      this.router.navigate(['/auth/login']);
    }

    this.form = this.fb.group({
      code: ['', [Validators.required, this.codeValidator]]
    });
  }

  // Validador personalizado para código
  codeValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    if (/\s/.test(value)) {
      return { noSpaces: 'El código no debe contener espacios' };
    }
    const validPattern = /^[A-Z0-9]{6}$/;
    if (!validPattern.test(value)) {
      return { invalidFormat: 'El código debe tener 6 caracteres (solo letras mayúsculas y números)' };
    }
    return null;
  }

  get code() {
    return this.form.get('code')!;
  }

  getCodeError(): string {
    if (this.code?.hasError('required')) {
      return 'El código de verificación es requerido';
    }
    if (this.code?.hasError('noSpaces')) {
      return 'El código no debe contener espacios';
    }
    if (this.code?.hasError('invalidFormat')) {
      return 'El código debe tener 6 caracteres (letras mayúsculas y números)';
    }
    return '';
  }

  // ✅ Verificar el código usando verifyCode
  submit() {
    if (this.form.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';

    const codigo = this.form.get('code')?.value;

    this.authService.verifyCode(this.correo, codigo).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.data) {  // true si el código es correcto
          // Guardar el código y el correo para usarlos en el cambio de contraseña
          this.sessionStorage.set('resetCode', codigo);
          this.sessionStorage.set('resetCorreo', this.correo);
          // Navegar al componente de cambio de contraseña
          this.router.navigate(['/auth/change-password'], {
            state: { correo: this.correo, code: codigo }
          });
        } else {
          this.errorMessage = 'Código incorrecto. Por favor, intenta de nuevo.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.message || 'Error al verificar el código. Intenta de nuevo.';
      }
    });
  }

  // ✅ Reenviar código (llama a forgotPassword)
  resendCode() {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.forgotPassword(this.correo).subscribe({
      next: () => {
        this.isLoading = false;
        alert('Se ha enviado un nuevo código a tu correo electrónico');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.message || 'Error al reenviar el código. Intenta de nuevo.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/auth/forgot-password']);
  }
}