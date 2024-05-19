export class User {
  uid: string;
  nickname: string;
  currentRid: string | null;
  experience: number;


  constructor(
    uid: string,
    nickname: string,
    currentRid: string | null,
    experience: number
  ) {
    this.uid = uid;
    this.nickname = nickname;
    this.currentRid = currentRid;
    this.experience = experience;
  }
}
