import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: false,
  templateUrl: './change-password.html',
  styleUrls: ['./change-password.css']
})
export class ChangePassword {
  form: FormGroup;
  email: string = '';
  code: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;
  
  // Propiedades para verificar requisitos de contraseña
  hasMinLength: boolean = false;
  hasUppercase: boolean = false;
  hasLowercase: boolean = false;
  hasNumber: boolean = false;
  hasSpecialChar: boolean = false;
  hasNoSpaces: boolean = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    const navigation = this.router.getCurrentNavigation();
    this.email = navigation?.extras?.state?.['email'] || sessionStorage.getItem('resetEmail') || '';
    this.code = navigation?.extras?.state?.['code'] || sessionStorage.getItem('resetCode') || '';
    
    if (!this.email || !this.code) {
      this.router.navigate(['/login']);
    }

    this.form = this.fb.group({
      password: ['', [Validators.required, this.passwordValidator.bind(this)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator.bind(this) });

    // Escuchar cambios en la contraseña para actualizar los requisitos visuales
    this.form.get('password')?.valueChanges.subscribe((value: string) => {
      this.updatePasswordRequirements(value);
    });
  }

  // Validador personalizado para contraseña
  passwordValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    
    if (!value) return null;
    
    const errors: any = {};
    
    if (/\s/.test(value)) {
      errors.noSpaces = true;
    }
    
    if (value.length < 8) {
      errors.minLength = true;
    }
    
    if (value.length > 20) {
      errors.maxLength = true;
    }
    
    if (!/[A-Z]/.test(value)) {
      errors.uppercase = true;
    }
    
    if (!/[a-z]/.test(value)) {
      errors.lowercase = true;
    }
    
    if (!/[0-9]/.test(value)) {
      errors.number = true;
    }
    
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(value)) {
      errors.specialChar = true;
    }
    
    return Object.keys(errors).length ? errors : null;
  }

  // Validador para confirmar que las contraseñas coinciden
  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    if (!confirmPassword) return null;
    
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  // Actualizar requisitos visuales
  updatePasswordRequirements(value: string) {
    if (!value) {
      this.hasMinLength = false;
      this.hasUppercase = false;
      this.hasLowercase = false;
      this.hasNumber = false;
      this.hasSpecialChar = false;
      this.hasNoSpaces = true;
      return;
    }
    
    this.hasMinLength = value.length >= 8;
    this.hasUppercase = /[A-Z]/.test(value);
    this.hasLowercase = /[a-z]/.test(value);
    this.hasNumber = /[0-9]/.test(value);
    this.hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);
    this.hasNoSpaces = !/\s/.test(value);
  }

  get password() {
    return this.form.get('password')!;
  }

  get confirmPassword() {
    return this.form.get('confirmPassword')!;
  }

  getPasswordError(): string {
    if (this.password?.hasError('required')) {
      return 'La contraseña es requerida';
    }
    if (this.password?.hasError('noSpaces')) {
      return 'La contraseña no debe contener espacios';
    }
    if (this.password?.hasError('minLength')) {
      return 'La contraseña debe tener al menos 8 caracteres';
    }
    if (this.password?.hasError('maxLength')) {
      return 'La contraseña no debe exceder los 20 caracteres';
    }
    if (this.password?.hasError('uppercase')) {
      return 'La contraseña debe contener al menos una letra mayúscula';
    }
    if (this.password?.hasError('lowercase')) {
      return 'La contraseña debe contener al menos una letra minúscula';
    }
    if (this.password?.hasError('number')) {
      return 'La contraseña debe contener al menos un número';
    }
    if (this.password?.hasError('specialChar')) {
      return 'La contraseña debe contener al menos un carácter especial';
    }
    return '';
  }

  getConfirmPasswordError(): string {
    if (this.confirmPassword?.hasError('required')) {
      return 'Debes confirmar tu contraseña';
    }
    if (this.form?.hasError('passwordMismatch') && this.confirmPassword?.touched) {
      return 'Las contraseñas no coinciden';
    }
    return '';
  }

  togglePasswordVisibility(field: string) {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else if (field === 'confirm') {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  submit() {
    if (this.form.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';

    const nuevaPassword = this.form.get('password')?.value;

    this.authService.changePassword(this.email, this.code, nuevaPassword).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.data) {
          alert('Contraseña cambiada exitosamente. Por favor, inicia sesión con tu nueva contraseña.');
          sessionStorage.removeItem('resetEmail');
          sessionStorage.removeItem('resetCode');
          this.router.navigate(['/login']);
        } else {
          this.errorMessage = 'Error al cambiar la contraseña. Intenta de nuevo.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Error al cambiar la contraseña. Intenta de nuevo.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/verify-code']);
  }
}