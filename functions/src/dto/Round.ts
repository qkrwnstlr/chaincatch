import { Question } from "./Question";

export class Round {
  rid: string;
  state: string;
  currentQuestion: number;
  lastQuestionList: Question[];

  constructor(
    rid: string,
    state: string,
    currentQuestion: number,
    lastQuestionList: Question[]
  ) {
    this.rid = rid;
    this.state = state;
    this.currentQuestion = currentQuestion;
    this.lastQuestionList = lastQuestionList;
  }
}
