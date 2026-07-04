import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-apply-leave',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './apply-leave.component.html',
  styleUrl: './apply-leave.component.css'
})
export class ApplyLeaveComponent {
   leaveForm: FormGroup;
  isLoading = false;
  errorMessage = '';


  constructor(private fb: FormBuilder, private apiService: ApiService, private router: Router) {
    this.leaveForm = this.fb.group({
      leaveType: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['', Validators.required]
    });
  }


  onSubmit() {
    if (this.leaveForm.invalid) return;
    this.isLoading = true;
    this.apiService.applyLeave(this.leaveForm.value).subscribe({
      next: () => {
        this.router.navigate(['/employee/leave-history']);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to apply leave';
      }
    });
  }

}
