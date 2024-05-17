export class User {
  uid: string;
  nickname: string;
  currentRid: string | null | undefined;

  constructor(
    uid: string,
    nickname: string,
    currentRid: string | null | undefined
  ) {
    this.uid = uid;
    this.nickname = nickname;
    this.currentRid = currentRid;
  }
}
