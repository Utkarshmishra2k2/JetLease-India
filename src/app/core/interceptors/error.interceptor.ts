import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { ApiError } from '../models/models';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const body = err.error as ApiError | undefined;
      const message = body?.message || err.statusText || 'Something went wrong. Please try again.';
      if (err.status === 0) {
        toast.error('Cannot reach the server. Check your connection and try again.');
      } else if (err.status !== 401) {
        toast.error(message);
      }
      return throwError(() => err);
    })
  );
};