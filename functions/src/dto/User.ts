export class User {
  uid: string;
  nickname: string;
  currentRid: string | null;
  experience: number;
  isOnline: boolean;

  constructor(
    uid: string,
    nickname: string,
    currentRid: string | null,
    experience: number,
    isOnline: boolean
  ) {
    this.uid = uid;
    this.nickname = nickname;
    this.currentRid = currentRid;
    this.experience = experience;
    this.isOnline = isOnline;
  }
}
