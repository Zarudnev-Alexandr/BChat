from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from src.db import Chat, User, ChatUser


async def get_chats(session: AsyncSession) -> list[Chat]:
    """Получение всех чатов"""
    result = await session.execute(select(Chat))
    return result.scalars().all()


async def add_chat(session: AsyncSession, **kwargs) -> Chat:
    """Add chat to db"""
    new_chat = Chat(**kwargs)
    session.add(new_chat)
    return new_chat


async def get_chat_users(session: AsyncSession, chat_id: int) -> list[User]:
    """Получить всех участников чата"""
    result = await session.execute(select(User).join(ChatUser).join(Chat).where(Chat.id == chat_id))
    return result.scalars().all()


async def get_chat(session: AsyncSession, id: int) -> Chat:
    """Получение чата по id"""
    result = await session.execute(select(Chat).where(Chat.id == id))
    return result.scalars().first()


async def add_chat_user(session: AsyncSession, **kwargs) -> ChatUser:
    """Добавление пользователя в чат"""
    new_chat_user = ChatUser(**kwargs)
    session.add(new_chat_user)
    return new_chat_user