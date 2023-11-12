# from fastapi import FastAPI, WebSocket, APIRouter, WebSocketDisconnect, Depends
# from sqlalchemy.ext.asyncio import AsyncSession
# from src.db import get_session, Message
# from src.schemas import CreateMessageSchema
# from src.utils import get_chat_messages
# from typing import List

# websocket_router: APIRouter = APIRouter()


# class ConnectionManager:
#     def __init__(self):
#         self.active_connections: List[WebSocket] = []

#     async def connect(self, websocket: WebSocket):
#         await websocket.accept()
#         self.active_connections.append(websocket)

#     def disconnect(self, websocket: WebSocket):
#         self.active_connections.remove(websocket)

#     async def send_message(self, message: str, websocket: WebSocket):
#         await websocket.send_text(message)

#     async def get_messages_from_chat(self, chat_id: int, websocket: WebSocket, session: AsyncSession = Depends(get_session)):
#         async for message in get_chat_messages(session, chat_id):
#             await websocket.send_json(message)

#     async def save_chat_message(self, message: Message):
#         session = get_session()
#         session.add(message)
#         await session.commit()
#         await session.close()


# manager = ConnectionManager()


# @websocket_router.websocket("/ws/{client_id}")
# async def websocket_endpoint(websocket: WebSocket, client_id: int):
#     await manager.connect(websocket)
#     try:
#         while True:
#             data = await websocket.receive_text()
#             await manager.send_message(f"Client {client_id}: {data}", websocket)
#     except Exception:
#         manager.disconnect(websocket)
