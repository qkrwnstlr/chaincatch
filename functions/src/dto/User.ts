export class User {
  uid: string;
  profileImg: number;
  nickname: string;
  currentRid: string | null;
  experience: number;
  isOnline: boolean;

  constructor(
    uid: string,
    nickname: string,
    profileImg: number,
    currentRid: string | null,
    experience: number,
    isOnline: boolean
  ) {
    this.uid = uid;
    this.nickname = nickname;
    this.profileImg = profileImg;
    this.currentRid = currentRid;
    this.experience = experience;
    this.isOnline = isOnline;
  }
}
