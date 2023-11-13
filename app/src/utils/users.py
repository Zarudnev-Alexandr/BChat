from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from fastapi.responses import StreamingResponse

from src.db import Chat, User, ChatUser


async def get_users(session: AsyncSession) -> list[User]:
    """Get all Users from db"""
    result = await session.execute(select(User))
    return result.scalars().all()


async def get_user_by_email(session: AsyncSession, email: str) -> dict | None:
    result = await session.execute(select(User).where(User.email == email))
    user = result.scalar_one_or_none()

    if user is not None:
        user_dict = user.__dict__
        del user_dict["_sa_instance_state"]
        return user_dict
    else:
        return None


async def get_user(session: AsyncSession, id: int) -> User:
    """Забираем одного пользователя по id из бд"""
    result = await session.execute(select(User).where(User.id == id))
    return result.scalars().first()


async def get_user_by_nickname(session: AsyncSession, nickname: str) -> User:
    """Забираем одного пользователя по никнейму из бд"""
    result = await session.execute(select(User).where(User.nickname == nickname))
    return result.scalars().first()


async def update_avatar_user(session: AsyncSession, user_id: int, imageUrl: str):
    """Добавит аватарку пользователя"""
    await session.execute(
        update(User).where(User.id == user_id).values(imageURL=imageUrl)
    )
    await session.commit()


async def add_user(session: AsyncSession, **kwargs) -> User:
    """Add User to db"""
    new_user = User(**kwargs)
    session.add(new_user)
    return new_user


async def get_all_chats_from_user(session: AsyncSession, user_id: int) -> list[Chat]:
    """Получить все чаты пользователя"""
    result = await session.execute(
        select(Chat).join(ChatUser).join(User).where(User.id == user_id)
    )
    return result.scalars().all()
