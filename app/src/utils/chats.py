from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from src.db import Chat, User, ChatUser


async def get_chat(session: AsyncSession, id: int) -> Chat:
    """Получение чата по id"""
    result = await session.execute(select(Chat).where(Chat.id == id))
    return result.scalars().first()


async def get_chats(session: AsyncSession, 
                    user_id: int,
                    limit: int,
                    offset: int,) -> list[Chat]:
    """Получение всех чатов, в которых состоит пользователь"""
    result = await session.execute(
        select(Chat)
        .join(ChatUser)
        .where(user_id == ChatUser.user_id)
        .offset(offset)
        .limit(limit)
    )
    return result.scalars().all()


async def check_user_for_chat_membership(
    session: AsyncSession, user_id: int, chat_id: int
) -> ChatUser:
    """Проверка на то, является ли пользователь членом какого-то чата.
    Используется, чтобы не показывать соощения, если пользователь не присоединился
    """
    result = await session.execute(
        select(ChatUser)
        .join(Chat)
        .where((ChatUser.user_id == user_id) & (Chat.id == chat_id))
    )
    return result.scalar()


async def add_chat_user(session: AsyncSession, **kwargs) -> ChatUser:
    """Добавление пользователя в чат"""
    new_chat_user = ChatUser(**kwargs)
    session.add(new_chat_user)
    return new_chat_user


async def remove_chat_user(session: AsyncSession, chat_id: int, user_id: int):
    """Удаление пользователя из чата"""
    chat_user = await session.execute(
        select(ChatUser).filter_by(chat_id=chat_id, user_id=user_id)
    )
    chat_user = chat_user.scalar()
    await session.delete(chat_user)


async def add_chat(session: AsyncSession, **kwargs) -> Chat:
    """Add chat to db"""
    new_chat = Chat(**kwargs)
    session.add(new_chat)
    return new_chat


async def remove_chat(session: AsyncSession, chat_id: int):
    """Удаление чата"""

    removed_chat = await session.execute(select(Chat).filter_by(id=chat_id))
    removed_chat = removed_chat.scalar()
    await session.delete(removed_chat)


async def get_chat_users(session: AsyncSession, 
                         chat_id: int,
                         limit: int,
                         offset: int,):
    """Получить всех участников чата с информацией об администраторах"""
    result = await session.execute(
        select(
            User.id,
            User.nickname,
            User.name,
            User.surname,
            User.imageURL,
            User.is_online,
            ChatUser.is_admin,
        )
        .join(ChatUser)
        .join(Chat)
        .where(Chat.id == chat_id)
        .offset(offset)
        .limit(limit)
    )
    users_data = []
    for user_id, nickname, name, surname, imageURL, is_online, is_admin in result:
        user_data = {
            "id": user_id,
            "nickname": nickname,
            "name": name,
            "surname": surname,
            "imageURL": imageURL,
            "is_online": is_online,
            "is_admin": is_admin,
        }
        users_data.append(user_data)
    return users_data
