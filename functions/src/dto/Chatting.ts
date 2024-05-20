export class Chatting {
  uid: string;
  nickname: string;
  content: string;

  constructor(uid: string, nickname: string, content: string) {
    this.uid = uid;
    this.nickname = nickname;
    this.content = content;
  }
}
