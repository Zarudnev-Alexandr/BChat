from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from .models import *


async def get_users(session: AsyncSession) -> list[User]:
  """Get all Users from db"""
  result = await session.execute(select(User))
  return result.scalars().all()

async def add_user(session: AsyncSession, 
                   nickname: str,
                   name: str,
                   surname: str) -> User:
  """Add User to db"""
  new_user = User(nickname=nickname, name=name, surname=surname)
  session.add(new_user)
  return new_user