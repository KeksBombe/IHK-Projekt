import {generationState} from './generationState.interface';

export interface Test {
  id: number;
  name: string;
  description: string;
  testCSV: string;
  environmentID?: number;
  storyID: number;
  generationState: generationState
}
