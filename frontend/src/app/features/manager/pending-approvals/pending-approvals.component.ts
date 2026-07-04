import { Component, OnInit } from '@angular/core';
import { LeaveResponse, PageResponse } from '../../../core/models';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pending-approvals',
  imports: [CommonModule],
  templateUrl: './pending-approvals.component.html',
  styleUrl: './pending-approvals.component.css'
})
export class PendingApprovalsComponent implements OnInit {
   leaves: PageResponse<LeaveResponse> | null = null;


  constructor(private apiService: ApiService) {}


  ngOnInit() {
    this.loadPendingLeaves();
  }


  loadPendingLeaves() {
    this.apiService.getPendingLeaves().subscribe(data => {
      this.leaves = data;
    });
  }


  approve(leaveId: number) {
    this.apiService.approveLeave(leaveId).subscribe(() => {
      this.loadPendingLeaves();
    });
  }


  reject(leaveId: number) {
    this.apiService.rejectLeave(leaveId).subscribe(() => {
      this.loadPendingLeaves();
    });
  }

}
