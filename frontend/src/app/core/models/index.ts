export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface Employee {
  id: number;
  name: string;
  email: string;
  department: string;
  role: string;
  managerId?: number;
  managerName?: string;
  createdAt: string;
}

export interface LeaveRequest {
  leaveType: string;
  startDate: string;
  endDate: string;
  reason: string;
}

export interface LeaveResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveType: string;
  startDate: string;
  endDate: string;
  numberOfDays: number;
  reason: string;
  status: string;
  managerComments?: string;
  reviewedByName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface EmployeeDashboard {
  totalRequests: number;
  approvedRequests: number;
  pendingRequests: number;
  rejectedRequests: number;
  recentActivity: LeaveResponse[];
}

export interface ManagerDashboard {
  totalEmployees: number;
  pendingApprovals: number;
  approvedRequests: number;
  rejectedRequests: number;
  recentActivity: LeaveResponse[];
}

export interface AuthState {
  isAuthenticated: boolean;
  user: Employee | null;
  token: string | null;
}