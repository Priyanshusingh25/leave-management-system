import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Employee } from '../../../core/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile',
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  employee: Employee | null = null;


  constructor(private apiService: ApiService) {}


  ngOnInit() {
    this.apiService.getEmployeeProfile().subscribe(data => {
      this.employee = data;
    });
  }

}
