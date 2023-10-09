from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from src.db import Message


async def get_message(session: AsyncSession, id: int):
    """Получение сообщение по id"""

    result = await session.execute(select(Message).where(Message.id == id))
    return result.scalars().first()


async def add_message(session: AsyncSession, **kwargs):
    """Добавление сообщения"""

    new_message = Message(**kwargs)
    session.add(new_message)
    await session.commit()
    return new_message


async def get_chat_messages(session: AsyncSession, chat_id: int) -> list[Message]:
    """Получить все сообщения в чате"""

    result = await session.execute(select(Message).where(Message.chat_id == chat_id))
    return result.scalars().all()
