import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';
import { SessionStorageService } from '../../core/services/storage/session-storage.service';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPassword {
  form: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private sessionStorage: SessionStorageService
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, this.emailValidator]]
    });
  }

  // Validador personalizado para email
  emailValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    if (/\s/.test(value)) {
      return { noSpaces: 'El correo no debe contener espacios' };
    }
    const validPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!validPattern.test(value)) {
      return { invalidFormat: 'Formato de correo inválido' };
    }
    const invalidChars = /[^a-zA-Z0-9@._-]/;
    if (invalidChars.test(value)) {
      return { invalidChars: 'El correo contiene caracteres no permitidos' };
    }
    return null;
  }

  get email() {
    return this.form.get('email');
  }

  getEmailError(): string {
    if (this.email?.hasError('required')) {
      return 'El correo electrónico es requerido';
    }
    if (this.email?.hasError('noSpaces')) {
      return 'El correo no debe contener espacios';
    }
    if (this.email?.hasError('invalidFormat')) {
      return 'Ingresa un correo válido (ejemplo: usuario@dominio.com)';
    }
    if (this.email?.hasError('invalidChars')) {
      return 'El correo solo puede contener letras, números, @, ., - y _';
    }
    return '';
  }

  submit() {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    const email = this.form.get('email')?.value;

    // El backend espera { correo: email }
    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading = false;
        // Guardar el correo en sessionStorage para la siguiente pantalla
        this.sessionStorage.set('resetCorreo', email);
        // Navegar a la pantalla de ingreso del código
        this.router.navigate(['auth/forgot-password-code'], {
          state: { correo: email }
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.message || 'Error al enviar el enlace de recuperación. Intenta de nuevo.';
      }
    });
  }
}