export class Question {
  qid: string;
  uid: string;
  answer: string;
  state: string;
  startTime: number | null;
  drawing: string | null;
  successorUid: string | null;
  successorNickname: string | null;

  constructor(
    qid: string,
    uid: string,
    answer: string,
    state: string,
    startTime: number | null,
    drawing: string | null,
    successorUid: string | null,
    successorNickname: string | null
  ) {
    this.qid = qid;
    this.uid = uid;
    this.answer = answer;
    this.state = state;
    this.startTime = startTime;
    this.drawing = drawing;
    this.successorUid = successorUid;
    this.successorNickname = successorNickname;
  }
}
