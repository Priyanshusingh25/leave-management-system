import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LeaveRequest, LeaveResponse, PageResponse, EmployeeDashboard, ManagerDashboard, Employee } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getEmployeeProfile(): Observable<Employee> {
    return this.http.get<Employee>(`${this.API_URL}/api/employees/profile`);
  }

  getEmployeeDashboard(): Observable<EmployeeDashboard> {
    return this.http.get<EmployeeDashboard>(`${this.API_URL}/api/employees/dashboard`);
  }

  applyLeave(request: LeaveRequest): Observable<LeaveResponse> {
    return this.http.post<LeaveResponse>(`${this.API_URL}/api/employees/leaves/apply`, request);
  }

  getLeaveHistory(page: number = 0, size: number = 10, status?: string, type?: string): Observable<PageResponse<LeaveResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) params = params.set('status', status);
    if (type) params = params.set('leaveType', type);
    
    return this.http.get<PageResponse<LeaveResponse>>(`${this.API_URL}/api/employees/leaves`, { params });
  }

  getLeaveById(id: number): Observable<LeaveResponse> {
    return this.http.get<LeaveResponse>(`${this.API_URL}/api/employees/leaves/${id}`);
  }

  updateLeave(id: number, request: LeaveRequest): Observable<LeaveResponse> {
    return this.http.put<LeaveResponse>(`${this.API_URL}/api/employees/leaves/${id}`, request);
  }

  cancelLeave(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/employees/leaves/${id}`);
  }

  getManagerDashboard(): Observable<ManagerDashboard> {
    return this.http.get<ManagerDashboard>(`${this.API_URL}/api/manager/dashboard`);
  }

  getPendingLeaves(page: number = 0, size: number = 10): Observable<PageResponse<LeaveResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<LeaveResponse>>(`${this.API_URL}/api/manager/pending-leaves`, { params });
  }

  getTeamLeaveHistory(page: number = 0, size: number = 10): Observable<PageResponse<LeaveResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<LeaveResponse>>(`${this.API_URL}/api/manager/team-leave-history`, { params });
  }

  approveLeave(id: number, comments?: string): Observable<LeaveResponse> {
    return this.http.put<LeaveResponse>(`${this.API_URL}/api/manager/leaves/${id}/approve`, { comments });
  }

  rejectLeave(id: number, comments?: string): Observable<LeaveResponse> {
    return this.http.put<LeaveResponse>(`${this.API_URL}/api/manager/leaves/${id}/reject`, { comments });
  }
}