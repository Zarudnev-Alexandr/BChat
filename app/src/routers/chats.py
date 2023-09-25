from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.ext.asyncio import AsyncSession
from src.db.database import get_session

from src.schemas import AllUsersProfilesMain, ChatCreate, ChatUserCreate, ChatBase
from src.utils import add_chat, add_chat_user, get_chat, get_chat_users, get_user, get_chats


chats_router: APIRouter = APIRouter()


@chats_router.get("/", response_model=list[ChatBase])
async def get_all_chats(session: AsyncSession = Depends(get_session)):
    """Все чаты"""
    chats = await get_chats(session)
    if chats:
        return chats
    else:
        return JSONResponse(content={"message": "Чатов нет, соцсеть сдохла :)"})


@chats_router.get("/{id}/users", response_model=list[AllUsersProfilesMain])
async def get_chat_members(chat_id: int, session: AsyncSession = Depends(get_session)):
    """Все пользователи одного чата"""
    chat = await get_chat(session, id=chat_id)
    if chat:
      users = await get_chat_users(session, chat_id)
      return users
    else:
        return JSONResponse(content={"message": f"Не найден чат с id {chat_id}"})
    

@chats_router.get("/{id}")
async def get_one_chat(id: int, session: AsyncSession = Depends(get_session)):
    """Получение чата по id"""
    chat = await get_chat(session, id)
    return chat


@chats_router.post("/add")
async def add_one_chat(chat: ChatCreate, session: AsyncSession = Depends(get_session)):
    """Добавление одного чата в бд"""
    chat = {"name": chat.name, "id_creator": chat.id_creator}
    print(chat)
    user = await get_user(session, id=chat["id_creator"])
    if not user:
        return JSONResponse(
            content={"message": f"Не найден пользователь с id {chat['id_creator']}"},
            status_code=400,
        )

    chatAdd = await add_chat(session, **chat)
    try:
        await session.commit()
        return chatAdd
    except Exception as e:
        print(e)
        await session.rollback()


@chats_router.post("/add/user/")
async def add_user_to_chat(chat_user: ChatUserCreate, session: AsyncSession = Depends(get_session)):
    """Добавление пользователя в чат"""
    chat_user = {"chat_id": chat_user.chat_id, 
                 "user_id": chat_user.user_id}
    user = await get_user(session, id=chat_user["user_id"])
    chat = await get_chat(session, id=chat_user["chat_id"])
    chatUserAdd = await add_chat_user(session, **chat_user)
    if user:
        if chat:
            try:
                await session.commit()
                return chatUserAdd
            except Exception as e:
              print(e.__dict__["orig"])
              await session.rollback()
              return JSONResponse(content={"message": "Не удалось добавить пользователя в чат"}, status_code=500)
        else:
            return JSONResponse(content={"message": f"Не найден чат с id {chat_user['chat_id']}"})            
    else:
        return JSONResponse(content={"message": f"Не найден пользователь с id {chat_user['user_id']}"})