from fastapi import WebSocket


class ConnectionManager:
    def __init__(self):
        self.active_connections: dict[int, dict[int, WebSocket]] = {}

    async def connect(self, chat_id: int, user_id: int, websocket: WebSocket):
        await websocket.accept()
        if chat_id not in self.active_connections:
            self.active_connections[chat_id] = {}
        self.active_connections[chat_id][user_id] = websocket

    def disconnect(self, chat_id: int, user_id: int):
        if chat_id in self.active_connections:
            self.active_connections[chat_id].pop(user_id, None)

    async def send_message(self, message: dict, chat_id: int):
        if chat_id in self.active_connections:
            for user_id, websocket in self.active_connections[chat_id].items():
                await websocket.send_json(message)

    async def receive_message(self, chat_id: int):
        if chat_id in self.active_connections:
            for _, websocket in self.active_connections[chat_id].items():
                await websocket.receive_json()



ws_manager = ConnectionManager()