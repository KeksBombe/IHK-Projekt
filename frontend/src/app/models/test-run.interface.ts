import {TestStatus} from './test-status.enum';

export interface TestRun {
  id: number;
  status: TestStatus;
  description: string;
  testJson?: string;
  testId: number;
  executedAt: string;
}
