import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPassword {
  form: FormGroup;

  constructor(private fb: FormBuilder, private router: Router ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, this.emailValidator]]
    });
  }

  // Validador personalizado para email
  emailValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    
    if (!value) return null;
    
    // No permitir espacios
    if (/\s/.test(value)) {
      return { noSpaces: 'El correo no debe contener espacios' };
    }
    
    // No permitir símbolos raros (solo letras, números, puntos, guiones, guión bajo y @)
    const validPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!validPattern.test(value)) {
      return { invalidFormat: 'Formato de correo inválido' };
    }
    
    // Validar que no tenga caracteres especiales no permitidos
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
    if (this.form.valid) {
      const email = this.form.get('email')?.value;
      console.log('Enviar enlace de recuperación a:', email);
      // Aquí va la lógica para enviar el email de recuperación
      alert(`Se ha enviado un enlace de recuperación a ${email}`);
      this.router.navigate(['auth/forgot-password-code'])
    }
  }
}