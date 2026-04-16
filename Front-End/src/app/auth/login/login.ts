import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth/auth.service';
import { LoadingService } from '../../core/services/api/interceptors/loading.interceptor';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})

export class Login implements OnInit {
  form!: FormGroup;
  globalMsg = '';
  isLoading = false;  // Controla el estado de carga visual

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private loadingService: LoadingService  // Opcional: para manejo global
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  submit(): void {
    this.globalMsg = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    // Opcional: activar loader global
    this.loadingService.show();

    this.authService.login(this.form.value)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.loadingService.hide();
        })
      )
      .subscribe({
        next: () => {
          this.globalMsg = '✅ Inicio de sesión exitoso. Redirigiendo...';
          setTimeout(() => this.router.navigate(['/dashboard']), 800);
        },
        error: (err) => {
          // El ErrorInterceptor ya transformó el error a ApiError
          this.globalMsg = err?.message || '❌ Error al iniciar sesión. Intenta de nuevo.';
        }
      });
  }

  // Verifica si un campo tiene error y ha sido tocado
  hasError(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!(control && control.invalid && control.touched);
  }

  // Obtiene el mensaje de error específico para el campo
  getError(controlName: string): string {
    const control = this.form.get(controlName);
    if (!control) return '';

    if (control.hasError('required')) return 'Este campo es obligatorio';
    if (control.hasError('email')) return 'Correo electrónico inválido';
    if (control.hasError('minlength')) return 'Mínimo 6 caracteres';

    return '';
  }
}