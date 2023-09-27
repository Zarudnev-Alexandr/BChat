from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from src.db import Chat, User, get_password_hash, create_access_token, pwd_context


async def get_users(session: AsyncSession) -> list[User]:
    """Get all Users from db"""
    result = await session.execute(select(User))
    return result.scalars().all()


async def login_user(session: AsyncSession, **kwargs):
    """Логин пользовалея"""

    result = await session.execute(select(User).where(User.email == kwargs["email"]))
    user =  result.scalars().first()
    if user is None or not pwd_context.verify(kwargs["password"], user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверное имя пользователя или пароль",
        )
    user_info = {
              "id": user.id,
              "nickname": user.nickname,
              "email": user.email
          }
    access_token = create_access_token(data=user_info)
    return {"user": user, "access_token": access_token}


async def get_user(session: AsyncSession, id: int) -> User:
    """Забираем одного пользователя по id из бд"""
    result = await session.execute(select(User).where(User.id == id))
    return result.scalars().first()


async def add_user(session: AsyncSession, **kwargs) -> User:
    """Add User to db"""
    new_user = User(**kwargs)
    session.add(new_user)
    return new_user


async def get_user_chats(session: AsyncSession, id: int) -> list[Chat]:
    """Получить все чаты пользователя"""
    result = await session.execute(select(Chat).where(Chat.id_creator == id))
    return result.scalars().all()