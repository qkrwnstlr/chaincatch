export class Question {
  answer: string;
  state: string;
  startTime: number;
  drawing: string | null | undefined;

  constructor(
    answer: string,
    state: string,
    startTime: number,
    drawing: string | null | undefined
  ) {
    this.answer = answer;
    this.state = state;
    this.startTime = startTime;
    this.drawing = drawing;
  }
}
