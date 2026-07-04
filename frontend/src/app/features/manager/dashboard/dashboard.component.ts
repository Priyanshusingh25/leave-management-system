import { Component, OnInit } from '@angular/core';
import { ManagerDashboard } from '../../../core/models';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
   dashboard: ManagerDashboard | null = null;


  constructor(private apiService: ApiService) {}


  ngOnInit() {
    this.apiService.getManagerDashboard().subscribe(data => {
      this.dashboard = data;
    });
  }

}
