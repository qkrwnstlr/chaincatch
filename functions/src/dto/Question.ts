export class Question {
  uid: string;
  answer: string;
  state: string;
  startTime: number | null;
  drawing: string | null;
  successorUid: string | null;

  constructor(
    uid: string,
    answer: string,
    state: string,
    startTime: number | null,
    drawing: string | null,
    successorUid: string | null,
  ) {
    this.uid = uid;
    this.answer = answer;
    this.state = state;
    this.startTime = startTime;
    this.drawing = drawing;
    this.successorUid = successorUid;
  }
}
