export class RoomDetail {
  rid: string;
  playerList: string[];
  watingList: string[];

  constructor(
    rid: string,
    playerList: string[],
    watingList: string[]
  ) {
    this.rid = rid;
    this.playerList = playerList;
    this.watingList = watingList;
  }
}
