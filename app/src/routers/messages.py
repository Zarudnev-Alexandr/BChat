from fastapi import APIRouter, Depends, WebSocket, WebSocketDisconnect, WebSocketException, status
from fastapi.responses import JSONResponse
from fastapi import HTTPException
from fastapi import Query
from typing import List
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker
from src.db import get_session, async_session
from typing import Annotated
from src.websockets import ws_manager

from src.utils import (
    get_current_user,
    get_message_by_id,
    add_message,
    get_chat,
    check_user_for_chat_membership,
    remove_message,
#     get_chat_users,
    get_messages_paged,
)

from fastapi import (
    APIRouter,
    Depends,
    Query,
    status,
    WebSocket,
    WebSocketDisconnect,
    WebSocketException,
)
from src.schemas import MessageSchema, CreateMessageSchema, EditMessageSchema

from src.websockets import ws_manager


messages_router: APIRouter = APIRouter()

@messages_router.post("/add/", response_model=MessageSchema)
async def post_message_func(message: CreateMessageSchema, current_user: dict = Depends(get_current_user)):
  """Отправка сообщения"""

  chat = await get_chat(current_user.session, message.chat_id)
  chat_member = await check_user_for_chat_membership(current_user.session, current_user.id, message.chat_id)

  if not chat:
    raise HTTPException(status_code=404, detail=f"Не обнаружен чат с id {message.chat_id}")
  
  if not chat_member:
    raise HTTPException(status_code=403, detail=f"Пользователь с id {current_user.id} не является участником чата с id {message.chat_id}")
  
  try:
    message = {
      "chat_id": message.chat_id,
      "sender_id": current_user.id,
      "text": message.text,
    }
    postMessage = await add_message(current_user.session, **message)
    await current_user.session.commit()
    if postMessage:
       return postMessage
  except:
    raise HTTPException(status_code=400, detail="Не удалось отправить сообщение")
  

@messages_router.get("/get/", response_model=List[MessageSchema])
async def get_messages_func(chat_id: int, 
                            limit: int = Query(default=20, ge=1, le=100),  # Ограничиваем limit от 1 до 100
                            offset: int = Query(default=0, ge=0), # offset должен быть больше или равен 0
                            current_user: dict = Depends(get_current_user)):
  """Получаем сообщения в конкретном чате"""
  
  chat = await get_chat(current_user.session, chat_id)
  chat_member = await check_user_for_chat_membership(current_user.session, current_user.id, chat_id)
  

  if not chat:
    raise HTTPException(status_code=404, detail=f"Не обнаружен чат с id {chat_id}")
  
  if not chat_member:
    raise HTTPException(status_code=403, detail=f"Пользователь с id {current_user.id} не является участником чата с id {chat_id}")
  
  # Вычисляем смещение (offset) для пагинации
  offset = offset * limit

  # Получаем сообщения с использованием пагинации
  messages = await get_messages_paged(current_user.session, chat_id, limit, offset)

  return messages


@messages_router.put("/edit/{message_id}/", response_model=MessageSchema)
async def edit_message_func(
    message_id: int, edited_message: EditMessageSchema, current_user: dict = Depends(get_current_user)
):
    """Редактирование сообщения"""

    message = await get_message_by_id(current_user.session, message_id)

    if not message:
        raise HTTPException(status_code=404, detail=f"Сообщение с id {message_id} не найдено")

    chat_member = await check_user_for_chat_membership(
        current_user.session, current_user.id, message.chat_id
    )

    if not chat_member:
        raise HTTPException(status_code=403, detail="Вы не являетесь участником чата данного сообщения")

    if message.sender_id != current_user.id:
        raise HTTPException(status_code=403, detail="Вы не можете редактировать чужие сообщения")

    try:
        # Обновляем только поле text
        message.text = edited_message.text
        message.is_edit = True

        await current_user.session.commit()
        return message
    except Exception as e:
        print(e)
        await current_user.session.rollback()
        raise HTTPException(status_code=400, detail="Не удалось отредактировать сообщение")


@messages_router.delete("/delete/{message_id}/")
async def delete_message_func(
   message_id: int, 
   current_user: dict = Depends(get_current_user)
):
   """Удаление сообщения"""

   message = await get_message_by_id(current_user.session, message_id)

   if not message:
        raise HTTPException(status_code=404, detail=f"Сообщение с id {message_id} не найдено")
   
   chat_member = await check_user_for_chat_membership(
        current_user.session, current_user.id, message.chat_id
    )
      
   if not chat_member:
        raise HTTPException(status_code=403, detail="Вы не являетесь участником чата данного сообщения")
   
   if (message.sender_id != current_user.id) and (chat_member.is_admin == False):
        raise HTTPException(status_code=403, detail="Вы не можете удалять чужие сообщения или вы не являетесь админом чата")
   
   try:
      await remove_message(current_user.session, message_id)
      await current_user.session.commit()
      return {"message": f"Сообщение с id {message_id} удалено из чата"}
   
   except Exception as e:
      print(e)
      await current_user.session.rollback()
      raise HTTPException(status_code=400, detail="Не удалось удалить сообщение")
   


websocket_router = APIRouter()





async def get_token(
    websocket: WebSocket,
    token: Annotated[str | None, Query()] = None,
):
    if token is None:
        raise WebSocketException(code=status.WS_1008_POLICY_VIOLATION)
    return token



@websocket_router.websocket("/chat/{chat_id}")
async def websocket_endpoint(
    websocket: WebSocket,
    chat_id: int,
    token: Annotated[str, Depends(get_token)] = Query(None),
):        
    async with async_session() as session:
       user = await get_current_user(session, token)
       chat = await get_chat(session, chat_id)
       chat_member = await check_user_for_chat_membership(session, user.id, chat_id)
    
    if not user:
        raise WebSocketException(code=status.WS_1003_UNSUPPORTED_DATA)
    
    if not chat:
       raise WebSocketException(code=status.WS_1003_UNSUPPORTED_DATA)
    
    if not chat_member:
       raise WebSocketException(code=status.WS_1011_INTERNAL_ERROR)

    try:
        await ws_manager.connect(chat_id, user.id, websocket)

        while True:
           data = await websocket.receive_text()
           if data:
              try:
                message = {
                  "chat_id": chat_id,
                  "sender_id": user.id,
                  "text": data,
                }
                postMessage = await add_message(session, **message)
                await session.commit()
                if postMessage:
                    # Отправка сообщения всем пользователям в чате
                    await ws_manager.send_message(
                        await ws_manager.send_message(
                          {"sender": str(user.nickname), "text": data},
                          chat_id,
                        ),
                        chat_id,
                    )
              except Exception as e:
                  print(f"Error processing message: {e}")


    except WebSocketDisconnect:
        ws_manager.disconnect(chat_id, user.id)
