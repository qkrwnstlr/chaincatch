export class User {
  uid: string;
  profileImg: number;
  nickname: string;
  nftAddress: string;
  currentRid: string | null;
  experience: number;
  isOnline: boolean;

  constructor(
    uid: string,
    nickname: string,
    profileImg: number,
    nftAddress: string,
    currentRid: string | null,
    experience: number,
    isOnline: boolean
  ) {
    this.uid = uid;
    this.nickname = nickname;
    this.profileImg = profileImg;
    this.nftAddress = nftAddress;
    this.currentRid = currentRid;
    this.experience = experience;
    this.isOnline = isOnline;
  }
}
