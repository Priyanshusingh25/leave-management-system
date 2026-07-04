import { Component, OnInit } from '@angular/core';
import { LeaveResponse, PageResponse } from '../../../core/models';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-team-history',
  imports: [CommonModule],
  templateUrl: './team-history.component.html',
  styleUrl: './team-history.component.css'
})
export class TeamHistoryComponent implements OnInit {
   leaves: PageResponse<LeaveResponse> | null = null;


  constructor(private apiService: ApiService) {}


  ngOnInit() {
    this.apiService.getTeamLeaveHistory().subscribe(data => {
      this.leaves = data;
    });
  }

}
