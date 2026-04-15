import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-forgot-password-code',
  standalone: false,
  templateUrl: './forgot-password-code.html',
  styleUrls: ['./forgot-password-code.css']
})
export class ForgotPasswordCode {
  form: FormGroup;
  email: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    // Recuperar el email del state o sessionStorage
    const navigation = this.router.getCurrentNavigation();
    this.email = navigation?.extras?.state?.['email'] || sessionStorage.getItem('resetEmail') || '';
    
    if (!this.email) {
      // Si no hay email, redirigir al login
      this.router.navigate(['/login']);
    }

    this.form = this.fb.group({
      code: ['', [Validators.required, this.codeValidator]]
    });
  }

  // Validador personalizado para código
  codeValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    
    if (!value) return null;
    
    // No permitir espacios
    if (/\s/.test(value)) {
      return { noSpaces: 'El código no debe contener espacios' };
    }
    
    // Solo permitir números y letras mayúsculas (6 caracteres)
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

  submit() {
    if (this.form.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';

    const codigo = this.form.get('code')?.value;

    this.authService.verifyCode(this.email, codigo).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.data) {
          // Guardar el código en sessionStorage para usarlo en cambio de contraseña
          sessionStorage.setItem('resetCode', codigo);
          // Navegar a la pantalla de cambio de contraseña
          this.router.navigate(['/change-password'], {
            state: { email: this.email, code: codigo }
          });
        } else {
          this.errorMessage = 'Código incorrecto. Por favor, intenta de nuevo.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Error al verificar el código. Intenta de nuevo.';
      }
    });
  }

  resendCode() {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.isLoading = false;
        alert('Se ha enviado un nuevo código a tu correo electrónico');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Error al reenviar el código. Intenta de nuevo.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/forgot-password']);
  }
}