from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc
from typing import List

from src.db import Message, User


async def add_message(session: AsyncSession, **kwargs):
    """Добавление сообщения"""

    new_message = Message(**kwargs)
    session.add(new_message)
    await session.commit()
    return new_message


async def get_messages_paged(
    session: AsyncSession, chat_id: int, limit: int, offset: int
) -> List[dict]:
    """Получить определенное количество сообщений в чате с использованием пагинации"""

    query = (
        select(
            Message.id,
            Message.text,
            Message.date_of_create,
            Message.sender_id,
            Message.is_edit,
            Message.chat_id,
            User.nickname,
        )
        .join(User)
        .where((Message.chat_id == chat_id) & (Message.sender_id == User.id))
        .order_by(desc(Message.date_of_create), desc(Message.id))
        .offset(offset)
        .limit(limit)
    )

    result = await session.execute(query)

    messages_data = []
    for (
        message_id,
        text,
        date_of_create,
        sender_id,
        is_edit,
        chat_id,
        nickname,
    ) in result:
        message_data = {
            "id": message_id,
            "text": text,
            "date_of_create": date_of_create,
            "sender_id": sender_id,
            "is_edit": is_edit,
            "chat_id": chat_id,
            "sender_nickname": nickname,
        }
        messages_data.append(message_data)

    return messages_data


async def get_message_by_id(session: AsyncSession, message_id: int) -> Message:
    """Получить сообщение по его идентификатору"""

    result = await session.execute(select(Message).where(Message.id == message_id))
    return result.scalar()


async def remove_message(session: AsyncSession, message_id: int):
    """Удаление чата"""

    removed_message = await session.execute(select(Message).filter_by(id=message_id))
    removed_message = removed_message.scalar()
    await session.delete(removed_message)
