import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'employee',
    canActivate: [AuthGuard],
    data: { role: 'EMPLOYEE' },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/employee/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'apply-leave',
        loadComponent: () => import('./features/employee/apply-leave/apply-leave.component').then(m => m.ApplyLeaveComponent)
      },
      {
        path: 'leave-history',
        loadComponent: () => import('./features/employee/leave-history/leave-history.component').then(m => m.LeaveHistoryComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/employee/profile/profile.component').then(m => m.ProfileComponent)
      }
    ]
  },
  {
    path: 'manager',
    canActivate: [AuthGuard],
    data: { role: 'MANAGER' },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/manager/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'pending-approvals',
        loadComponent: () => import('./features/manager/pending-approvals/pending-approvals.component').then(m => m.PendingApprovalsComponent)
      },
      {
        path: 'team-history',
        loadComponent: () => import('./features/manager/team-history/team-history.component').then(m => m.TeamHistoryComponent)
      }
    ]
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: '404',
    loadComponent: () => import('./shared/components/not-found/not-found.component').then(m => m.NotFoundComponent)
  },
  {
    path: '**',
    redirectTo: '/404'
  }
];