export class Room {
  rid: string;
  title: string;
  manager: string;
  maxUser: number;
  currentUser: number;
  state: string;

  constructor(
    rid: string,
    title: string,
    manager: string,
    maxUser: number,
    currentUser: number,
    state: string,
  ) {
    this.rid = rid;
    this.title = title;
    this.manager = manager;
    if (maxUser == null || undefined) {
      this.maxUser = 5;
    } else {
      this.maxUser = maxUser;
    }
    if (maxUser == null || undefined) {
      this.currentUser = 1;
    } else {
      this.currentUser = currentUser;
    }
    this.state = state;
  }
}
