from fastapi import FastAPI, Depends, WebSocketException, status, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi import FastAPI, WebSocket, APIRouter
from typing import List

import src

app = FastAPI()

origins = ["http://localhost", "http://localhost:8080", "http://localhost:3000", "*"]
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def db_init_models():
    """Initial db models"""
    await src.init_models()
    print("Done")


app.include_router(src.users_router, prefix="/api/users", tags=["Users"])

app.include_router(src.chats_router, prefix="/api/chats", tags=["Chats"])

app.include_router(src.messages_router, prefix="/api/messages", tags=["Messages"])

# app.include_router(
#     src.websocket_router,
#     prefix="/chat"
# )

# websocket_router: APIRouter = APIRouter()


# class ConnectionManager:
#     def __init__(self, session: AsyncSession) -> None:
#         self.active_connections = []
#         self.session = session
#         # self.active_connections: list[str,WebSocket]= {}
#         # print("Creating a list to hold active connections",self.active_connections)

#     async def connect(self, chat_id: int, websocket: WebSocket):
#         await websocket.accept()
#         # if not self.active_connections.get(chat_id):
#         #     self.active_connections[chat_id] = []

#         # #Получаем все сообщения из этого чата
#         # async with Depends(src.get_session) as session:
#         #   chat_messages = await src.get_chat_messages(session, chat_id)

#         # # Отправляем историю чата новому пользователю
#         # for message in chat_messages:
#         #     await websocket.send_text(message.text)

#         self.active_connections.append(websocket)
#         print("New Active connections are ",self.active_connections)

#     async def receive_and_broadcast(self, websocket: WebSocket, chat_id: int):
#         while True:
#             data = await websocket.receive_json()
#             user_id = data.get("user_id")
#             message_text = data.get("message_text")
#             if user_id and message_text:
#                 async with self.session:
#                     new_message = src.Message(chat_id=chat_id, sender_id=user_id, text=message_text)
#                     self.session.add(new_message)

#                 for connection in self.active_connections:
#                     if connection != websocket:
#                         await connection.send_json({"user_id": user_id, "message_text": message_text})


#     async def disconnect(self, chat_id: str, websocket: WebSocket):
#         self.active_connections[chat_id].remove(websocket)
#         print("After disconnect active connections are: ",self.active_connections)


#     # async def send_personal_message(self, message: str, websocket: WebSocket):
#     #     await websocket.send_text(message)
#     #     print("Sent a personal msg to , ",websocket)

#     # async def broadcast(self, message: str, chat_id: str, websocket: WebSocket):
#     #     for connection in self.active_connections[chat_id]:
#     #         if connection != websocket:
#     #             await connection.send_text(message)
#     #             print("In broadcast: sent msg to ",connection)

# manager = ConnectionManager(src.get_session())


# # @app.websocket("/{chat_id}")
# # async def websocket_chat(websocket: WebSocket, chat_id: str):
# #     await manager.connect(chat_id, websocket)
# #     try:
# #         while True:
# #             data = await websocket.receive_text()
# #             await manager.send_personal_message(f"You wrote: {data}",websocket)
# #             await manager.broadcast(f"A client says: {data}", chat_id, websocket)
# #     except Exception as e:
# #         print("Got an exception ",e)
# #         await manager.disconnect(chat_id, websocket)

# @app.websocket('/ws')
# async def websocket_chat(websocket: WebSocket, chat_id: int):
#     await manager.connect(chat_id, websocket)
#     try:
#         await manager.receive_and_broadcast(websocket, chat_id)
#     except Exception as e:
#         print(e)
#     finally:
#         await manager.disconnect(websocket)


class ConnectionManager:
    def __init__(self):
        self.active_connections: dict[int, WebSocket] = {}

    async def connect(self, user_id: int, websocket: WebSocket):
        await websocket.accept()
        self.active_connections[user_id] = websocket

    def disconnect(self, user_id: int):
        self.active_connections.pop(user_id)

    async def send_personal_message(self, message: dict, user_id: int):
        if websocket := self.active_connections.get(user_id):
            await websocket.send_json(message)


ws_manager = ConnectionManager()


@app.websocket("/ws/send_message/{chat_id}/{user_id}/")
async def websocket_send_message(
    websocket: WebSocket,
    session: src.get_session,
    chat_id: int,
    user_id: int,
):
    chat = await src.get_chat(session, chat_id)
    user = await src.get_user(session, user_id)

    if not user:
        print("Пользователя нет")
    if not chat:
        print("Чата нет")
    try:
        await ws_manager.connect(user.id, websocket)
        await ws_manager.send_personal_message(
            {"message": "connection accepted"},
            user.id,
        )
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        ws_manager.disconnect(user.id)
