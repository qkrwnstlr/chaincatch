export class Question {
  uid: string;
  answer: string;
  state: string;
  startTime: number;
  drawing: string | null;

  constructor(
    uid: string,
    answer: string,
    state: string,
    startTime: number,
    drawing: string | null
  ) {
    this.uid = uid;
    this.answer = answer;
    this.state = state;
    this.startTime = startTime;
    this.drawing = drawing;
  }
}
