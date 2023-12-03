from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update

from src.db import Chat, User, ChatUser


async def get_users(session: AsyncSession,
                    limit: int,
                    offset: int,) -> list[User]:
    """Get all Users from db"""
    result = await session.execute(select(User)
                                   .offset(offset)
                                   .limit(limit))
    return result.scalars().all()


async def get_user_by_email(session: AsyncSession, email: str):
    result = await session.execute(select(User).where(User.email == email))
    user = result.scalar_one_or_none()

    if user is not None:
        return user
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
