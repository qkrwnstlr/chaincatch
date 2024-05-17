export class RoomDetail {
  rid: string;
  state: string;
  currentRound: number;
  playerList: string[];
  watingList: string[];

  constructor(
    rid: string,
    state: string,
    currentRound: number,
    playerList: string[],
    watingList: string[]
  ) {
    this.rid = rid;
    this.state = state;
    this.currentRound = currentRound;
    this.playerList = playerList;
    this.watingList = watingList;
  }
}
