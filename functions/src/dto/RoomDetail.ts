export class RoomDetail {
  rid: string;
  playerList: string[];
  waitingList: string[];
  playerCount: PlayerCountManager;

  constructor(
    rid: string,
    playerList: string[],
    waitingList: string[],
    playerCount: PlayerCountManager
  ) {
    this.rid = rid;
    this.playerList = playerList;
    this.waitingList = waitingList;
    this.playerCount = playerCount;
  }
}

export class PlayerCountManager {
  [key: string]: any;

  constructor(initialData: { [key: string]: number } = {}) {
    Object.assign(this, initialData);
  }

  set(key: string, value: number) {
    this[key] = value;
  }

  delete(key: string): boolean {
    if (Object.hasOwnProperty.call(this, key)) {
      delete this[key];
      return true;
    }
    return false;
  }
}
