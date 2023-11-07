from fastapi import FastAPI, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi import FastAPI, WebSocket

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

app.include_router(src.bootcamps_router, prefix="/api/bootcamps", tags=["Bootcamps"])


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
