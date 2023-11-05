from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc
from typing import List

from src.db import Message


async def add_message(session: AsyncSession, **kwargs):
    """Добавление сообщения"""

    new_message = Message(**kwargs)
    session.add(new_message)
    await session.commit()
    return new_message


async def get_messages_paged(session: AsyncSession, chat_id: int, limit: int, offset: int) -> List[Message]:
    """Получить определенное количество сообщений в чате с использованием пагинации"""

    # Используем метод slice для применения пагинации к результату запроса, сортируем по убыванию времени и по убыванию id
    query = select(Message).where(Message.chat_id == chat_id).order_by(desc(Message.date_of_create), desc(Message.id)).offset(offset).limit(limit)

    result = await session.execute(query)
    
    # Возвращаем список сообщений в виде объектов Message
    return result.scalars().all()


async def get_message_by_id(session: AsyncSession, message_id: int) -> Message:
    """Получить сообщение по его идентификатору"""

    result = await session.execute(select(Message).where(Message.id == message_id))
    return result.scalar()

async def remove_message(session: AsyncSession, message_id: int):
    """Удаление чата"""

    removed_message = await session.execute(
        select(Message).filter_by(id=message_id)
    )
    removed_message = removed_message.scalar()
    await session.delete(removed_message)
