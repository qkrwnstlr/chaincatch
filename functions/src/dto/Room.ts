export class Room {
  rid: string | null | undefined;
  title: string;
  manager: string;
  maxUser: number;
  currentUser: number;

  constructor(
    rid: string | null | undefined,
    title: string,
    manager: string,
    maxUser: number,
    currentUser: number
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
  }
}
