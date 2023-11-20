from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc, delete, or_, not_, and_
from sqlalchemy.sql import func
from math import sqrt
from geopy.distance import geodesic
from typing import List

from src.db import Bootcamp, BootcampRoles, User

async def get_bootcamps(session: AsyncSession, user_latitude: float, user_longitude: float, limit: int, offset: int) -> List[Bootcamp]:
    """Получить все буткемпы с учетом расстояния от пользователя"""

    # Выбираем все буткемпы
    query = select(Bootcamp)
    result = await session.execute(query)    
    bootcamps = result.scalars().all()

    # Сортируем буткемпы по расстоянию от пользователя
    bootcamps.sort(key=lambda bootcamp: geodesic((user_latitude, user_longitude), (bootcamp.geoposition_latitude, bootcamp.geoposition_longitude)).meters)

    # Применяем пагинацию
    return bootcamps[offset:offset+limit]


async def get_bootcamps_admin(session: AsyncSession, user_id: int, user_latitude: float, user_longitude: float, limit: int, offset: int) -> list[Bootcamp]:
    """Получить все буткемпы, где пользователь является админом"""

    query = select(Bootcamp).join(BootcampRoles).where(
        (BootcampRoles.user_id == user_id) & (BootcampRoles.role == "админ")
    )
    result = await session.execute(query)    
    bootcamps = result.scalars().all()

    bootcamps.sort(key=lambda bootcamp: geodesic((user_latitude, user_longitude), (bootcamp.geoposition_latitude, bootcamp.geoposition_longitude)).meters)

    # Применяем пагинацию
    return bootcamps[offset:offset+limit]


async def get_bootcamps_member(session: AsyncSession, user_id: int, user_longitude: float, user_latitude: float, limit: int, offset: int) -> list[Bootcamp]:
    """Получить все буткемпы, где пользователь является админом"""

    query = select(Bootcamp).join(BootcampRoles).where(
        (BootcampRoles.user_id == user_id) & (BootcampRoles.role == "участник")
    ).order_by(desc(Bootcamp.start_time), desc(Bootcamp.id)).offset(offset).limit(limit)

    result = await session.execute(query)    
    return result.scalars().all()

async def add_bootcamp(session, **kwargs) -> Bootcamp:
    """Создать буткемп"""

    new_bootcamp = Bootcamp(**kwargs)
    session.add(new_bootcamp)
    await session.commit()
    await session.refresh(new_bootcamp)
    return new_bootcamp

async def get_bootcamp(session: AsyncSession, bootcamp_id: int) -> Bootcamp:
    """Получить буткемп по id"""
    bootcamp = await session.execute(select(Bootcamp).where(Bootcamp.id == bootcamp_id))
    return bootcamp.scalar()


async def remove_bootcamp(session: AsyncSession, bootcamp_id: int):
    """Удаление буткемпа"""

    removed_bootcamp = await session.execute(
        select(Bootcamp).filter_by(id=bootcamp_id)
    )
    removed_bootcamp = removed_bootcamp.scalar()
    print("removed_bootcamp:", removed_bootcamp)  # Добавьте этот отладочный вывод
    if removed_bootcamp:
        await session.delete(removed_bootcamp)
        await session.commit()
        return True
    else:
        return False

async def check_bootcamp_membership(session: AsyncSession, user_id: int, bootcamp_id: int) -> BootcampRoles:
    """Проверка на участие пользователя в буткемпе"""

    result = await session.execute(
        select(BootcampRoles)
        .join(Bootcamp)
        .where((BootcampRoles.user_id == user_id) & (Bootcamp.id == bootcamp_id))
    )
    return result.scalar()


async def add_bootcamp_role(session: AsyncSession, **kwargs) -> BootcampRoles:
    """Добавить заявку на буткемп"""

    new_bootcamp_role = BootcampRoles(**kwargs)
    session.add(new_bootcamp_role)
    await session.commit()
    await session.refresh(new_bootcamp_role)
    return new_bootcamp_role


async def remove_bootcamp_role(session: AsyncSession, bootcamp_id: int):
    """Удаление людей с буткемпа"""

    await session.execute(
        delete(BootcampRoles).where(BootcampRoles.bootcamp_id == bootcamp_id)
    )
    await session.commit()


async def get_bootcamp_applications(session: AsyncSession, bootcamp_id: int):
    """Получение всех заявок на буткемп"""

    result = await session.execute(
        select(BootcampRoles)
        .where((BootcampRoles.role == "ожидание") & (BootcampRoles.bootcamp_id == bootcamp_id))
    )
    return result.scalars().all()
    

async def get_bootcamp_application_by_id(session: AsyncSession, application_id: int):
    """Получение одной конкретной роли по id"""

    result = await session.execute(
        select(BootcampRoles)
        .where(BootcampRoles.id == application_id)
    )

    return result.scalar()


async def get_bootcamp_members(session: AsyncSession, bootcamp_id: int):
    """Получение всех участников буткемпа"""
    
    # Получите заявки, исключая те, которые имеют роль "отклонено"
    result = await session.execute(
        select(
            BootcampRoles.role,
            User.nickname,
            User.id,
            BootcampRoles.id.label('application_id')
        )
        .join(User)
        .where(and_(
            BootcampRoles.role.in_(["участник", "админ"]),
            BootcampRoles.bootcamp_id == bootcamp_id,
            User.id == BootcampRoles.user_id
        ))
        .where(not_(BootcampRoles.role.in_(["отклонено", "ожидание"])))
    )
    
    # Преобразуем результат в список словарей
    rows = result.all()
    keys = result.keys()
    return [dict(zip(keys, row)) for row in rows]
