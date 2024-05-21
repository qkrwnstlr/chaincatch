export class RoomDetail {
  rid: string;
  playerList: string[];
  waitingList: string[];

  constructor(
    rid: string,
    playerList: string[],
    waitingList: string[]
  ) {
    this.rid = rid;
    this.playerList = playerList;
    this.waitingList = waitingList;
  }
}
