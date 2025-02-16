import { onRequest } from "firebase-functions/v2/https";
import * as admin from "firebase-admin";
import * as database from "firebase-functions/v2/database";

import * as express from "express";

import OpenAI from "openai";

import { User } from "./dto/User";
import { Room } from "./dto/Room";
import { PlayerCountManager, RoomDetail } from "./dto/RoomDetail";
import { Chatting } from "./dto/Chatting";
import { Round } from "./dto/Round";
import { Question } from "./dto/Question";

const app = express();

admin.initializeApp();

/**
 * 내부 함수 호출
 */
const callCloudFunction = async (functionName: string, data: object = {}) => {
  const url = `https://app-mqqqc7oa4q-uc.a.run.app/${functionName}`;
  await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
};

/**
 * chatGpt 호출
 */
let chatGptMessages: OpenAI.Chat.Completions.ChatCompletionMessageParam[] = [
  {
    role: "system",
    content: "앞으로 너의 역할은 무작위 단어를 말하는 거야.",
  },
  {
    role: "system",
    content: "너는 앞으로 단어 하나만 말할 수 있어.",
  },
];

const callChatGpt = async (content: string): Promise<string> => {
  const openai = new OpenAI({
    apiKey: "",
  });
  if (chatGptMessages.length > 1000) {
    chatGptMessages = [
      {
        role: "system",
        content: "앞으로 너의 역할은 무작위 단어를 말하는 거야.",
      },
      {
        role: "system",
        content: "너는 앞으로 단어 하나만 말할 수 있어.",
      },
    ];
  }
  chatGptMessages.push({
    role: "user",
    content: content,
  });
  const completion = await openai.chat.completions.create({
    model: "gpt-3.5-turbo",
    temperature: 1,
    presence_penalty: 2,
    messages: chatGptMessages,
  });
  const [choice] = completion.choices;
  const message = choice.message.content ?? "error";
  chatGptMessages.push({
    role: "assistant",
    content: message,
  });

  return choice.message.content ?? "error";
};

/**
 * 범위 사이의무작위 숫자 생성
 */
const getRandomNumber = (min: number, max: number) => {
  return Math.floor(Math.random() * (max - min + 1)) + min;
};

/**
 * 사용자가 처음으로 접속했을 때
 * https://app-mqqqc7oa4q-uc.a.run.app/createUser
 */
app.post("/createUser", async (req, res) => {
  const user = req.body as User;
  user.experience = 1;
  user.isOnline = true;
  await admin.database().ref(`/user/${user.uid}`).update(user);
  res.status(200).send();
});

/**
 * 사용자 삭제
 * https://app-mqqqc7oa4q-uc.a.run.app/deleteUser
 */
app.post("/deleteUser", async (req, res) => {
  const uid = req.body["uid"] as string;
  await admin.database().ref(`/user/${uid}`).remove();
  res.status(200).send();
});

/**
 * 사용자가 연결이 끊어졌을 때
 * callback
 * 1. 사용자의 firebase connection이 끊기면 동작
 * 2. exitRoom 호출
 */
exports.onUserDisconnected = database.onValueUpdated(
  "/user/{uid}/isOnline",
  async (event) => {
    const isOnline = event.data.after.val() as boolean;
    if (!isOnline) {
      return callCloudFunction("/exitRoom", { uid: event.params.uid });
    }
    return null;
  }
);

/**
 * 사용자가 방을 만들때
 * https://app-mqqqc7oa4q-uc.a.run.app/createRoom
 * 1. room 생성
 * 2. roomDetail 생성
 * 3. chatting 생성 및 system chatting 추가
 * 4. user currentRid 설정
 */
app.post("/createRoom", async (req, res) => {
  const rid = admin.database().ref(`/room`).push().key!;

  // room 생성
  const room = req.body as Room;
  room.rid = rid;
  room.state = "Waiting";
  room.maxUser = 5;
  room.currentUser = 1;
  await admin.database().ref(`/room/${rid}`).update(room);

  // roomDetail 생성
  const playerCount = new PlayerCountManager();
  playerCount[room.manager] = 0;
  // playerCount.set(room.manager, 0);
  const roomDetail = new RoomDetail(rid, [room.manager], [], playerCount);
  await admin.database().ref(`/roomDetail/${rid}`).set(roomDetail);

  // Chatting 생성
  const systemChatting = new Chatting(
    "System",
    "System",
    "방이 생성되었습니다."
  );
  await admin.database().ref(`chatting/${rid}`).push(systemChatting);

  // manager의 currentRid 설정
  const userRef = admin.database().ref(`/user/${room.manager}`);
  await userRef.child(`/currentRid`).ref.set(`${rid}`);

  res.status(200).send(rid);
});

/***
 * 사용자가 방에 들어올 때
 * https://app-mqqqc7oa4q-uc.a.run.app/enterRoom
 * 1. 현재 room이 Waiting 상태이면 바로 playingList에 추가
 * 2. 현재 room이 Playing 상태이면 watingList에 추가
 * 3. room의 현재 사용자 수 증가
 * 4. user에 currentRid 설정
 */
app.post("/enterRoom", async (req, res) => {
  const rid = req.body["rid"] as string;
  const uid = req.body["uid"] as string;

  const roomStateRef = admin.database().ref(`/room/${rid}/state`);
  const state = (await roomStateRef.get()).val() as string;

  const roomDetailRef = admin.database().ref(`/roomDetail/${rid}`);
  const roomDetail = (await roomDetailRef.get()).val() as RoomDetail;

  // room이 Playing 상태이면
  if (state == "Playing") {
    // WaitingList에 user 추가
    if (roomDetail.waitingList) {
      roomDetail.waitingList = [...roomDetail.waitingList, uid];
    } else {
      roomDetail.waitingList = [uid];
    }
  }

  // room이 Waiting 상태이면
  if (state == "Waiting") {
    // playerList에 user 추가
    if (roomDetail.playerList) {
      roomDetail.playerList.push(uid);
    } else {
      roomDetail.playerList = [uid];
    }
    roomDetail.playerCount[uid] = 0;
    // roomDetail.playerCount.set(uid, 0);
  }

  await roomDetailRef.update(roomDetail);

  // room의 현재 사용자 수 증가
  const currentUserRef = admin.database().ref(`/room/${rid}/currentUser`);
  const currentUser = (await currentUserRef.get()).val() as number;
  await currentUserRef.set(currentUser + 1);

  // user에 현재 방 추가
  await admin.database().ref(`/user/${uid}/currentRid`).set(rid);

  res.status(200).send();
});

/**
 * 사용자가 방에서 나갈 때
 * https://exitroom-mqqqc7oa4q-uc.a.run.app
 * 1. user에서 현재 방 정보 삭제
 * 2. 만약 room이 Playing 상태이면 playerList에서는 empty로만 변경
 * 3. 만약 room이 Waiting 상태이면 playerList에서 삭제
 * 4. waitingList에서 삭제
 * 4. 방장이 나갔으면 전체 정보 삭제
 */
app.post("/exitRoom", async (req, res) => {
  const uid = req.body["uid"] as string;
  const curretnRidSnapshot = await admin
    .database()
    .ref(`/user/${uid}/currentRid`)
    .get();

  // 현재 참여중인 방이 없으면 바로 종료
  if (!curretnRidSnapshot.exists()) return;

  const rid = curretnRidSnapshot.val() as string;

  // user에서 현재 방 삭제
  await admin.database().ref(`/user/${uid}/currentRid`).remove();

  const roomRef = admin.database().ref(`/room/${rid}`);
  const room = (await roomRef.get()).val() as Room;

  const roomDetailRef = admin.database().ref(`/roomDetail/${rid}`);
  const roomDetail = (await roomDetailRef.get()).val() as RoomDetail;

  // PlayerList에서 삭제
  if (room.state == "Playing") {
    if (roomDetail.playerList) {
      roomDetail.playerList = roomDetail.playerList.map((value) =>
        value == uid ? "empty" : value
      );
    }
  }

  if (room.state == "Waiting") {
    // roomDetail에서 사용자 삭제
    if (roomDetail.playerList) {
      roomDetail.playerList = roomDetail.playerList.filter(
        (value) => value != uid
      );
      delete roomDetail.playerCount[uid];
      // roomDetail.playerCount.delete(uid);
    }

    // room의 currentUser 감소
    await roomRef.child("currentUser").set(room.currentUser - 1);
  }

  // WaitingList에서 삭제
  if (roomDetail.waitingList) {
    roomDetail.waitingList = roomDetail.waitingList.filter(
      (value) => value != uid
    );
  }
  await roomDetailRef.update(roomDetail);

  // 방장이 나가면 다 삭제
  if (room.manager == uid) {
    await roomRef.child(`state`).set("Finished");
    await admin.database().ref(`/drawing/${rid}`).remove();
    await admin.database().ref(`/question/${rid}`).remove();
    await admin.database().ref(`/round/${rid}`).remove();
    await admin.database().ref(`/chatting/${rid}`).remove();
    await roomDetailRef.remove();
    await roomRef.remove();
  }

  res.status(200).send();
});

/**
 * roomDetail이 삭제될때
 * callback
 * 1. playerList의 사용자들에게서 currentRid 삭제
 * 2. waitingList의 사용자들에게서 currentRid 삭제
 */
exports.onRoomDetailDeleted = database.onValueDeleted(
  "/roomDetail/{rid}",
  async (event) => {
    const roomDetail = event.data.val() as RoomDetail;
    if (roomDetail.playerList) {
      roomDetail.playerList.forEach(async (uid) => {
        await admin.database().ref(`/user/${uid}/currentRid`).remove();
      });
    }
    if (roomDetail.waitingList) {
      roomDetail.waitingList.forEach(async (uid) => {
        await admin.database().ref(`/user/${uid}/currentRid`).remove();
      });
    }
  }
);

/**
 * 방장이 게임을 시작할 때
 * https://app-mqqqc7oa4q-uc.a.run.app/startGame
 * 1. round 생성
 * 2. room의 상태를 Playing으로 변경
 */
app.post("/startGame", async (req, res) => {
  const rid = req.body["rid"] as string;

  // round 생성
  const round = new Round(rid, "Waiting", 0, []);
  await admin.database().ref(`round/${rid}`).update(round);

  // room 상태를 Playing으로 변경
  await admin.database().ref(`/room/${rid}/state`).set("Playing");

  res.status(200).send();
});

/**
 * room 상태가 바꼈을 때
 * 1. Waiting이면 empty 사용자들 다 삭제
 * 2. Waiting이면 waitingList를 playingList에 옮김
 */
exports.onRoomStateChanged = database.onValueUpdated(
  "/room/{rid}/state",
  async (event) => {
    const state = event.data.after.val() as string;
    const rid = event.params.rid;

    if (state == "Waiting") {
      const roomDetailRef = admin.database().ref(`/roomDetail/${rid}`);
      const roomDetailSnapShot = await roomDetailRef.get();
      if (!roomDetailSnapShot.exists()) return;

      // empty 사용자 전부 삭제
      const roomDetail = roomDetailSnapShot.val() as RoomDetail;
      roomDetail.playerList = roomDetail.playerList.filter(
        (value) => value != "empty"
      );

      // waitingList -> playerList
      if (roomDetail.waitingList) {
        roomDetail.waitingList.forEach(
          (uid) => (roomDetail.playerCount[uid] = 0)
          // roomDetail.playerCount.set(uid, 0)
        );
        roomDetail.playerList = [
          ...roomDetail.playerList,
          ...roomDetail.waitingList,
        ];
        roomDetail.waitingList = [];
      }

      await admin.database().ref(`/roomDetail/${rid}`).update(roomDetail);

      // room의 현재 사용자 setting
      const currentUserRef = admin
        .database()
        .ref(`/room/${event.params.rid}/currentUser`);
      return currentUserRef.set(roomDetail.playerList.length);
    }

    return null;
  }
);

/**
 * round 상태가 바꼈을 때
 * 1. finished면 30초 후 round 삭제 및 room 상태를 Waiting으로 변경 및 system chatting 추가
 * 2. round state가 Waiting이 되고 아직 다 안돌았을 때 5초후 question을 Waiting으로 추가
 * 3. 다 돌았으면 finished로 변경
 */
exports.onRoundStateChanged = database.onValueWritten(
  "/round/{rid}/state",
  async (event) => {
    const state = event.data.after.val() as string;

    const roundRef = event.data.after.ref.parent!;
    const round = (await roundRef.get()).val() as Round;

    if (state == "Finished") {
      const roomRef = admin.database().ref(`/room/${event.params.rid}`);

      // 30초 후에
      // setTimeout(async () => {
      //   // round 삭제
      //   await roundRef.remove();
      //   // room의 상태를 Waiting으로 변경
      //   await roomRef.child("state").ref.set("Waiting");
      // }, 30000);

      // 5초 후에
      setTimeout(async () => {
        // round 삭제
        await roundRef.remove();
        // room의 상태를 Waiting으로 변경
        await roomRef.child("state").ref.set("Waiting");
      }, 2000);

      // 시스템 채팅 추가
      const chattingRef = admin.database().ref(`/chatting/${event.params.rid}`);
      const chatting = new Chatting(
        "System",
        "System",
        "Round가 종료되었습니다."
      );

      return chattingRef.push(chatting);
    }

    const roomDetailRef = admin
      .database()
      .ref(`/roomDetail/${event.params.rid}`);
    const roomDetail = (await roomDetailRef.get()).val() as RoomDetail;

    if (state == "Waiting") {
      if (round.currentQuestion < roomDetail.playerList.length) {
        const questionRef = admin
          .database()
          .ref(`/question/${event.params.rid}`);

        while (roomDetail.playerList[round.currentQuestion] == "empty") {
          round.currentQuestion++;
        }

        if (round.currentQuestion >= roomDetail.playerList.length) {
          return roundRef.child("state").set("Finished");
        }

        await roundRef.update(round);

        let answer = await callChatGpt(
          `${getRandomNumber(2, 7)}글자의 단어 하나만 말해줘.`
        );

        answer = answer.replace(/\./g, "");

        const question = new Question(
          Math.random().toString().substring(2, 8),
          roomDetail.playerList[round.currentQuestion],
          answer,
          "Waiting",
          null,
          null,
          null,
          null
        );

        return setTimeout(async () => {
          await questionRef.update(question);
        }, 2000);
      }

      if (round.currentQuestion == roomDetail.playerList.length) {
        return setTimeout(async () => {
          await roundRef.child("state").set("Finished");
        }, 2000);
      }
    }

    return null;
  }
);

/**
 * question이 추가되었을 때
 * callback
 * 1. System chatting 추가
 * 2. 5초 후 Playing 상태로 변경
 * 3. 125초 후 Fail 상태로 변경
 */
exports.onQuestionCreated = database.onValueCreated(
  "/question/{rid}",
  async (event) => {
    const question = event.data.val() as Question;
    const userRef = admin.database().ref(`/user/${question.uid}`);
    const user = (await userRef.get()).val() as User;
    const chattingRef = admin.database().ref(`/chatting/${event.params.rid}`);

    // 시스템 채팅 추가
    const chatting = new Chatting(
      "System",
      "System",
      `${user.nickname}님의 차례입니다.`
    );
    chattingRef.push(chatting);

    // 5초 후에 상태를 Playing으로 변경
    setTimeout(async () => {
      const questionRef = admin.database().ref(`/question/${event.params.rid}`);
      const questionSnapshot = await questionRef.get();
      if (questionSnapshot.exists()) {
        const question = questionSnapshot.val() as Question;
        question.state = "Playing";
        question.startTime = Date.now();
        event.data.ref.update(question);
      }
    }, 2000);

    // 120초 후에 아직도 남아있으면 상태를 Fail로 변경
    setTimeout(async () => {
      const questionRef = admin.database().ref(`/question/${event.params.rid}`);
      const questionSnapshot = await questionRef.get();
      if (questionSnapshot.exists()) {
        const currentQuestion = questionSnapshot.val() as Question;
        if (
          question.uid == currentQuestion.uid &&
          question.qid == currentQuestion.qid
        ) {
          event.data.child(`state`).ref.set("Fail");
        }
      }
    }, 122000);

    return null;
  }
);

/**
 * question이 삭제되었을 때
 * callback
 * 1. round state를 Waiting으로 변경
 * 2. lastQuestionList에 question 추가
 */
exports.onQuestionDeleted = database.onValueDeleted(
  "/question/{rid}",
  async (event) => {
    const roundRef = admin.database().ref(`/round/${event.params.rid}`);

    const question = event.data.val() as Question;

    // round를 Waiting으로 변경
    const round = (await roundRef.get()).val() as Round;
    round.state = "Waiting";

    // lastQuestionList에 값 추가
    if (round.lastQuestionList) {
      round.lastQuestionList = [...round.lastQuestionList, question];
    } else {
      round.lastQuestionList = [question];
    }

    return roundRef.update(round);
  }
);

/**
 * question 상태가 변경되었을 때
 * callback
 * 1. playing 상태일 시 round 상태도 playing으로 변경 및 currentQuestion 증가
 * 2. 성공시 question 삭제
 * 3. 실패시 system chatting 추가
 * 4. 실패시 question 삭제
 */
exports.onQuestionStateUpdated = database.onValueUpdated(
  "/question/{rid}/state",
  async (event) => {
    const state = event.data.after.val() as string;
    const chattingRef = admin.database().ref(`/chatting/${event.params.rid}`);
    switch (state) {
      case "Playing": {
        const roundRef = admin.database().ref(`/round/${event.params.rid}`);
        const round = (await roundRef.get()).val() as Round;
        round.state = "Playing";
        round.currentQuestion++;

        return roundRef.update(round);
      }
      case "Success": {
        return setTimeout(async () => {
          const drawingRef = admin
            .database()
            .ref(`/drawing/${event.params.rid}`);
          await drawingRef.remove();
          await event.data.after.ref.parent?.remove();
        }, 2000);
      }
      case "Fail": {
        const chatting = new Chatting(
          "System",
          "System",
          "시간이 초과되었습니다."
        );
        await chattingRef.push(chatting);
        return setTimeout(async () => {
          const drawingRef = admin
            .database()
            .ref(`/drawing/${event.params.rid}`);
          await drawingRef.remove();
          await event.data.after.ref.parent?.remove();
        }, 2000);
      }
    }

    return null;
  }
);

/**
 * chatting이 추가되었을 때
 * callback
 * 1. 정답이면 question 상태를 success로 변경
 * 2. 정답이면 successorUid를 맞힌 사람으로 변경
 * 3. 정답이면 drawing을 당시의 drawing으로 변경
 * 4. 시스템 채팅 추가
 * 5. 맞힌 사람 경험치 증가
 */
exports.onChattingAdded = database.onValueCreated(
  "/chatting/{rid}/{cid}",
  async (event) => {
    const chatting = event.data.val() as Chatting;
    const questionRef = admin.database().ref(`/question/${event.params.rid}`);
    const question = (await questionRef.get()).val() as Question;
    const drawingRef = admin.database().ref(`/drawing/${event.params.rid}`);
    const drawing = (await drawingRef.get()).val() as string;

    if (chatting.nickname == "System") return null;

    // 정답이면
    if (chatting.content == question.answer) {
      // successorUid를 맞힌 사람으로 변경
      question.successorUid = chatting.uid;
      question.successorNickname = chatting.nickname;
      // question의 상태를 Success로 변경
      question.state = "Success";
      // drawing을 당시의 drawing으로 변경
      question.drawing = drawing;
      await questionRef.update(question);

      // 시스템 채팅 추가
      const systemChatting = new Chatting(
        "System",
        "System",
        `${chatting.nickname} 님이 ${question.answer}를 맞혔습니다.`
      );

      await event.data.ref.parent?.push(systemChatting);

      // 맞힌 사람 경험치 증가
      const expRef = admin.database().ref(`/user/${chatting.uid}/experience`);
      const exp = (await expRef.get()).val() as number;
      return expRef.set(exp + 1);
    }

    return null;
  }
);

exports.app = onRequest(app);
