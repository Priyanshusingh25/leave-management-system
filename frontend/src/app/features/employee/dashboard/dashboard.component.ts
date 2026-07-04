import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { EmployeeDashboard } from '../../../core/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
 dashboard: EmployeeDashboard | null = null;


  constructor(private apiService: ApiService) {}


  ngOnInit() {
    this.apiService.getEmployeeDashboard().subscribe(data => {
      this.dashboard = data;
    });
  }
}
