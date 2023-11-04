from fastapi import APIRouter, Depends

from fastapi.responses import JSONResponse
from fastapi import HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from src.db.database import get_session

from src.schemas import (
    AllUsersProfilesMain,
    ChatCreate,
    ChatUserCreate,
    ChatBase,
    UserChatMember,
)
from src.utils import (
    add_chat,
    add_chat_user,
    get_chat,
    get_chat_users,
    get_user,
    get_chats,
    get_current_user,
    check_user_for_chat_membership,
    remove_chat_user,
    remove_chat    
)

CHAT_NOT_FOUND = "Такого чата не существует"
USER_NOT_FOUND = "Такой пользователь не найден"
USER_ALREADY_IN_CHAT = "Пользователь уже является участником данного чата"
USER_NOT_IN_CHAT = "Пользователь не состоит в чате"

chats_router: APIRouter = APIRouter()


@chats_router.get("/", response_model=list[ChatBase])
async def get_chats_func(current_user: dict = Depends(get_current_user)):
    """Все чаты пользователя"""
    chats = await get_chats(current_user["session"], current_user["id"])
    if chats:
        return chats
    else:
        raise HTTPException(status_code=404, detail=f"У пользователя с id {current_user['id']} нет чатов")


@chats_router.get("/{chat_id}/users/", response_model=list[UserChatMember])
async def get_chat_members(
    chat_id: int, current_user: dict = Depends(get_current_user)
):
    """Все пользователи одного чата"""

    chat = await check_user_for_chat_membership(
        current_user["session"], current_user["id"], chat_id
    )  # Является ли пользователь участником чата
    users = await get_chat_users(current_user["session"], chat_id)

    if not chat:
        raise HTTPException(status_code=403, detail=USER_NOT_IN_CHAT)

    if not users:
        raise HTTPException(status_code=403, detail="Участников нет, чат пуст")

    return users


@chats_router.post("/add/user/")
async def add_user_to_chat(
    chat_user: ChatUserCreate, current_user: dict = Depends(get_current_user)
):
    """Добавление пользователя в чат"""

    chat_user = {
        "chat_id": chat_user.chat_id,
        "user_id": chat_user.user_id,
        "is_admin": chat_user.is_admin,
    }
    chat = await get_chat(current_user["session"], chat_user["chat_id"])
    chat_adder_member = await check_user_for_chat_membership(
        current_user["session"], current_user["id"], chat_user["chat_id"]
    )
    chat_member = await check_user_for_chat_membership(
        current_user["session"], chat_user["user_id"], chat_user["chat_id"]
    )
    user = await get_user(current_user["session"], id=chat_user["user_id"])

    if not chat:
        raise HTTPException(status_code=404, detail=CHAT_NOT_FOUND)

    if not chat_adder_member:
        raise HTTPException(status_code=403, detail="Вы не являетесь участником чата, вы не можете добавлять пользователей")

    if not user:
        raise HTTPException(status_code=404, detail=USER_NOT_FOUND)

    if not chat_member:
        try:
            chatUserAdd = await add_chat_user(current_user["session"], **chat_user)
            await current_user["session"].commit()
            return chatUserAdd
        except Exception as e:
            print(e)
            await current_user["session"].rollback()
            raise HTTPException(status_code=400, detail="Не удалось добавить пользователя в чат")
    else:
        raise HTTPException(status_code=400, detail=USER_ALREADY_IN_CHAT)


@chats_router.post("/add/")
async def add_chat_func(
    chat: ChatCreate, current_user: dict = Depends(get_current_user)
):
    """Создание нового чата"""

    chat = {"name": chat.name}
    chatAdd = await add_chat(current_user["session"], **chat)
    try:
        await current_user["session"].commit()
        if chatAdd:
            chat_user = {
                "chat_id": chatAdd.__dict__["id"],
                "user_id": current_user["id"],
                "is_admin": True,
            }
            add_creator = await add_chat_user(current_user["session"], **chat_user)
            await current_user["session"].commit()
            return add_creator
    except Exception as e:
        print(e)
        await current_user["session"].rollback()


@chats_router.delete("/{chat_id}/leave/")
async def leave_chat_func(chat_id: int, current_user: dict = Depends(get_current_user)):
    """Пользователь покидает чат"""

    chat = await get_chat(current_user["session"], chat_id)
    chat_member = await check_user_for_chat_membership(
        current_user["session"], current_user["id"], chat_id
    )

    if not chat:
        raise HTTPException(status_code=404, detail=CHAT_NOT_FOUND)

    if chat_member:
        try:
            await remove_chat_user(current_user["session"], chat_id, current_user["id"])
            await current_user["session"].commit()
            return {"message": "Пользователь успешно вышел из чата"}
        except Exception as e:
            print(e)
            await current_user["session"].rollback()
            raise HTTPException(status_code=400, detail="Не удалось удалить пользователя из чата")
    else:
        raise HTTPException(status_code=400, detail="Вы не являетесь участником чата, чтобы выйти из него")
    

@chats_router.delete("/{chat_id}/remove-user/{user_id}/")
async def leave_chat_func(chat_id: int, user_id: int, current_user: dict = Depends(get_current_user)):
    """Пользователя удаляют из чата"""

    chat = await get_chat(current_user["session"], chat_id)
    chat_deleter_member = await check_user_for_chat_membership(
        current_user["session"], current_user["id"], chat_id
    )
    user = await get_user(current_user["session"], user_id)#Удаляемый пользователь
    deleter_user = await check_user_for_chat_membership(
        current_user["session"], user_id, chat_id
    )

    if not chat:
        raise HTTPException(status_code=404, detail=CHAT_NOT_FOUND)
    
    if not chat_deleter_member:
        raise HTTPException(status_code=403, detail="Вы не являетесь участником данного чата")

    if not chat_deleter_member.is_admin:
        raise HTTPException(status_code=403, detail="Вы не являетесь админом чата и не можете удалять пользователей")
    
    if not user:
        raise HTTPException(status_code=404, detail="Удаляемого пользователя не существует")
    
    if not deleter_user:
        raise HTTPException(status_code=404, detail="Удаляемый пользователь не является участником данного чата")
    
    if user_id == current_user["id"]:
        raise HTTPException(status_code=400, detail="Вы не можете удалить самого себя, используйте другое API (/users/leave/)")
    
    if deleter_user.is_admin:
        raise HTTPException(status_code=403, detail="Удаляемый пользователь является админом, вы не можете его удалить")
    
    try:
        await remove_chat_user(current_user["session"], chat_id, user_id)
        await current_user["session"].commit()
        return {"message": f"Пользователь с id {user_id} удален из чата админом с id {current_user['id']}"}
    
    except Exception as e:
        print(e)
        await current_user["session"].rollback()
        raise HTTPException(status_code=400, detail="Не удалось удалить пользователя из чата")


@chats_router.delete("/{chat_id}/delete/")
async def delete_chat_func(chat_id: int, current_user: dict = Depends(get_current_user)):
    """Админ удаляет чат"""

    chat = await get_chat(current_user["session"], chat_id)
    chat_member = await check_user_for_chat_membership(
        current_user["session"], current_user["id"], chat_id
    )
    chat_members = await get_chat_users(current_user["session"], chat_id)

    if not chat:
        raise HTTPException(status_code=404, detail=CHAT_NOT_FOUND)
    
    if not chat_member:
        raise HTTPException(status_code=403, detail=USER_NOT_IN_CHAT)
    
    if not chat_member.is_admin:
        raise HTTPException(status_code=403, detail="Вы не являетесь админом, нет полномочий для удаления чата")
    
    try:
        if chat_members:
            for user in chat_members:
                user_id = user["id"]
                await remove_chat_user(current_user["session"], chat_id, user_id)
                await current_user["session"].commit()

        await remove_chat(current_user["session"], chat_id)
        await current_user["session"].commit()
        
        return {"message": f"Админ с id {current_user['id']} удалил чат с id {chat_id}"}
    except Exception as e:
        print(e)
        await current_user["session"].rollback()
        raise HTTPException(status_code=400, detail="Не удалось удалить чат")
