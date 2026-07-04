import { Component, OnInit } from '@angular/core';
import { LeaveResponse, PageResponse } from '../../../core/models';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-leave-history',
  imports: [CommonModule],
  templateUrl: './leave-history.component.html',
  styleUrl: './leave-history.component.css'
})
export class LeaveHistoryComponent implements OnInit {
leaves: PageResponse<LeaveResponse> | null = null;


  constructor(private apiService: ApiService) {}


  ngOnInit() {
    this.apiService.getLeaveHistory().subscribe(data => {
      this.leaves = data;
    });
  }
}
