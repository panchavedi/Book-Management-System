import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401) {
        localStorage.removeItem('bms_access_token');
        localStorage.removeItem('bms_user');
        location.href = '/login';
      }
      return throwError(() => e);
    })
  );
