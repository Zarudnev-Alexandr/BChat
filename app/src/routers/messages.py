from fastapi import APIRouter, Depends, WebSocket, WebSocketDisconnect
from fastapi.responses import JSONResponse
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List
from src.db import get_session

# from src.utils import (
#     get_message,
#     add_message,
#     get_chat,
#     get_chat_users,
#     get_chat_messages,
# )
from src.schemas import MessageSchema, CreateMessageSchema
from src.db import Message


messages_router: APIRouter = APIRouter()


# @messages_router.get("/{id}", response_model=MessageSchema)
# async def get_one_message(id: int, session: AsyncSession = Depends(get_session)):
#     """Получение одного сообщения по id"""

#     message = await get_message(session, id)
#     if message:
#         return message
#     else:
#         return JSONResponse(content={"message": f"Не найдено сообщение с id {id}"})


# @messages_router.post("/add")
# async def add_one_message(
#     message: CreateMessageSchema, session: AsyncSession = Depends(get_session)
# ):
#     """Отправка сообщения"""
#     chat = await get_chat(session, message.chat_id)
#     chat_user = await get_chat_users(session, message.chat_id)
#     if chat:
#         if message.sender_id in [cu.id for cu in chat_user]:
#             message = {
#                 "text": message.text,
#                 "sender_id": message.sender_id,
#                 "chat_id": message.chat_id,
#             }
#             messageAdd = await add_message(session, **message)
#             try:
#                 await session.commit()
#                 return messageAdd
#             except Exception as e:
#                 print(e.__dict__["orig"])
#                 await session.rollback()
#             return JSONResponse(
#                 content={"message": "Не удалось отправить сообщение"}, status_code=400
#             )
#         else:
#             return JSONResponse(
#                 content={
#                     "message": f"Пользователь с id {message.sender_id} не находится в чате с id {message.chat_id}"
#                 },
#                 status_code=400,
#             )
#     else:
#         return JSONResponse(
#             content={"message": f"Чата с id {message.chat_id} не существует"},
#             status_code=404,
#         )


# @messages_router.get("/chat/{chat_id}/all")
# async def get_all_messages_from_chat(
#     chat_id: int, session: AsyncSession = Depends(get_session)
# ):
#     """Получение всех сообщений конкретного чата"""

#     chat = await get_chat(session, chat_id)
#     if chat:
#         messages = await get_chat_messages(session, chat_id)
#         if messages:
#             return messages
#         else:
#             return JSONResponse(
#                 content={"message": "Нет сообщений, чат пуст"}, status_code=400
#             )
#     else:
#         return JSONResponse(
#             content={"message": f"Чата с id {chat_id} не существует"}, status_code=404
#         )
