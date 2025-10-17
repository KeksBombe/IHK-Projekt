import {inject, Injectable, OnDestroy} from '@angular/core';
import {BehaviorSubject, catchError, EMPTY, forkJoin, interval, map, of, Subject} from 'rxjs';
import {switchMap, takeUntil} from 'rxjs/operators';
import {Test} from '../models';
import {TestService} from './test.service';
import {generationState} from '../models/generationState.interface';

@Injectable({
  providedIn: 'root'
})
export class PollingService implements OnDestroy {

  private readonly trackedTests = new Map<number, Test>();
  private readonly trackedKeys$ = new BehaviorSubject<Set<number>>(new Set());
  private readonly destroy$ = new Subject<void>();

  private readonly pollIntervalMs = 2000;

  private testService = inject(TestService);

  constructor() {
    this.initializePollingStream();
  }

  public addTest(test: Test): void {
    if (!test?.id || this.trackedKeys$.value.has(test.id)) return;

    this.trackedTests.set(test.id, test);

    const updatedKeys = new Set(this.trackedKeys$.value);
    updatedKeys.add(test.id);
    this.trackedKeys$.next(updatedKeys);
  }

  public removeTest(test: Test): void {
    if (!test?.id || !this.trackedKeys$.value.has(test.id)) return;

    this.trackedTests.delete(test.id);
    const updatedKeys = new Set(this.trackedKeys$.value);
    updatedKeys.delete(test.id);
    this.trackedKeys$.next(updatedKeys);
  }

  private initializePollingStream(): void {
    this.trackedKeys$.pipe(
      switchMap(keys => {
        if (keys.size === 0) {
          return EMPTY;
        }
        return interval(this.pollIntervalMs);
      }),
      switchMap(() => this.pollAllTests()),
      takeUntil(this.destroy$)
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.trackedKeys$.complete();
  }

  private pollAllTests() {
    const tests = Array.from(this.trackedTests.values());

    if (tests.length === 0) {
      return of(null);
    }

    const testPolls$ = tests.map(test => this.fetchAndUpdateTest(test.id));
    return forkJoin(testPolls$).pipe(
      catchError(err => {
        return of(null);
      })
    );
  }

  private fetchAndUpdateTest(id: number) {
    const localTest = this.trackedTests.get(id);
    if (!localTest) return of(null);

    return this.testService.getTestById(id).pipe(
      map(response => response.body),
      switchMap(remoteTest => {
        if (!remoteTest) return of(null);
        this.updateTest(localTest, remoteTest);
        if (remoteTest.generationState === generationState.COMPLETED || remoteTest.generationState === generationState.FAILED) {
          this.removeTest(remoteTest);
        }
        return of(null);
      }),
      catchError(err => of(null))
    )
  }

  private updateTest(localTest: Test, remoteTest: Test): void {
    const {id, ...updates} = remoteTest;
    Object.entries(updates).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        (localTest as any)[key] = value;
      }
    });
  }

}
