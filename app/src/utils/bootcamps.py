from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc, delete, or_, not_, and_
from sqlalchemy.sql import func
from math import sqrt
from geopy.distance import geodesic
from typing import List

from .more_utils import get_coordinates_from_dadata
from src.db import Bootcamp, BootcampRoles, User, BootcampRolesEnum


async def get_bootcamps(
    session: AsyncSession,
    user_latitude: float,
    user_longitude: float,
    limit: int,
    offset: int,
):
    """Получить все буткемпы с учетом расстояния от пользователя и количеством участников"""

    # Выбираем все буткемпы и подсчитываем количество участников
    query = (
        select(
            Bootcamp,
            func.count(BootcampRoles.id).label("current_count_member")
        )
        .join(BootcampRoles, BootcampRoles.bootcamp_id == Bootcamp.id, isouter=True)
        .filter(
            or_(
                BootcampRoles.role == BootcampRolesEnum.admin,
                BootcampRoles.role == BootcampRolesEnum.member,
            )
        )
        .group_by(Bootcamp.id)
    )

    result = await session.execute(query)
    bootcamps = result.all()

    # Преобразуем результат в словарь
    bootcamps_dict = [
        {
            "id": bootcamp.id,
            "geoposition_longitude": bootcamp.geoposition_longitude,
            "geoposition_latitude": bootcamp.geoposition_latitude,
            "address": bootcamp.address,
            "visible_address": bootcamp.visible_address,
            "start_time": bootcamp.start_time,
            "end_time": bootcamp.end_time,
            "budget": bootcamp.budget,
            "members_count": bootcamp.members_count,
            "description": bootcamp.description,
            "current_members_count": count_members,
        }
        for bootcamp, count_members in bootcamps
    ]

    # Сортируем буткемпы по расстоянию от пользователя
    bootcamps_dict.sort(
        key=lambda b: geodesic(
            (user_latitude, user_longitude),
            (b['geoposition_latitude'], b['geoposition_longitude']),
        ).meters
    )

    # Применяем пагинацию
    return bootcamps_dict[offset : offset + limit]


async def get_bootcamps_admin(
    session: AsyncSession,
    user_id: int,
    user_latitude: float,
    user_longitude: float,
    limit: int,
    offset: int,
) -> list[Bootcamp]:
    """Получить все буткемпы, где пользователь является админом"""

    # Выбираем буткемпы, где пользователь является админом
    query = (
        select(Bootcamp)
        .join(BootcampRoles)
        .where((BootcampRoles.user_id == user_id) & (BootcampRoles.role == BootcampRolesEnum.admin))
    )
    result = await session.execute(query)
    bootcamps = result.scalars().all()

    # Сортируем буткемпы по расстоянию от пользователя
    bootcamps.sort(
        key=lambda bootcamp: geodesic(
            (user_latitude, user_longitude),
            (bootcamp.geoposition_latitude, bootcamp.geoposition_longitude),
        ).meters
    )

    # Добавляем поле current_members_count и считаем количество участников с ролью "member"
    for bootcamp in bootcamps:
        member_count_query = (
            select(func.count())
            .where((BootcampRoles.bootcamp_id == bootcamp.id) & (BootcampRoles.role == BootcampRolesEnum.member))
        )
        member_count_result = await session.execute(member_count_query)
        bootcamp.current_members_count = member_count_result.scalar() + 1  # Прибавляем 1 для учета админа

    # Применяем пагинацию
    return bootcamps[offset : offset + limit]


async def get_bootcamps_member(
    session: AsyncSession,
    user_id: int,
    user_latitude: float,
    user_longitude: float,
    limit: int,
    offset: int,
) -> list[Bootcamp]:
    """Получить все буткемпы, где пользователь является админом"""

    # Выбираем буткемпы, где пользователь является админом
    query = (
        select(Bootcamp)
        .join(BootcampRoles)
        .where((BootcampRoles.user_id == user_id) & (BootcampRoles.role == BootcampRolesEnum.member))
    )
    result = await session.execute(query)
    bootcamps = result.scalars().all()

    # Сортируем буткемпы по расстоянию от пользователя
    bootcamps.sort(
        key=lambda bootcamp: geodesic(
            (user_latitude, user_longitude),
            (bootcamp.geoposition_latitude, bootcamp.geoposition_longitude),
        ).meters
    )

    # Добавляем поле current_members_count и считаем количество участников с ролью "member"
    for bootcamp in bootcamps:
        member_count_query = (
            select(func.count())
            .where((BootcampRoles.bootcamp_id == bootcamp.id) & (BootcampRoles.role == BootcampRolesEnum.member))
        )
        member_count_result = await session.execute(member_count_query)
        bootcamp.current_members_count = member_count_result.scalar() + 1  # Прибавляем 1 для учета админа

    # Применяем пагинацию
    return bootcamps[offset : offset + limit]


async def add_bootcamp(session, **kwargs) -> Bootcamp:
    """Создать буткемп"""

    # Используем API Dadata для преобразования адреса в координаты
    address = kwargs.get("address")
    coordinates = await get_coordinates_from_dadata(address)

    # Обновляем координаты в аргументах функции
    kwargs["geoposition_latitude"] = coordinates.get("geoposition_latitude")
    kwargs["geoposition_longitude"] = coordinates.get("geoposition_longitude")

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

    removed_bootcamp = await session.execute(select(Bootcamp).filter_by(id=bootcamp_id))
    removed_bootcamp = removed_bootcamp.scalar()
    if removed_bootcamp:
        await session.delete(removed_bootcamp)
        await session.commit()
        return True
    else:
        return False


async def leave_bootcamp(session: AsyncSession, user_id: int, bootcamp_id: int):
    """Покидание буткемпа"""

    leaved_nootcamp = await session.execute(
        select(BootcampRoles).where(
            (BootcampRoles.bootcamp_id == bootcamp_id)
            & (BootcampRoles.user_id == user_id)
        )
    )
    leaved_nootcamp = leaved_nootcamp.scalar()
    if leaved_nootcamp:
        await session.delete(leaved_nootcamp)
        await session.commit()
        return True
    else:
        return False


async def check_bootcamp_membership(
    session: AsyncSession, user_id: int, bootcamp_id: int
) -> BootcampRoles:
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
        select(BootcampRoles).where(
            (BootcampRoles.role == "ожидание")
            & (BootcampRoles.bootcamp_id == bootcamp_id)
        )
    )
    return result.scalars().all()


async def get_bootcamp_application_by_id(session: AsyncSession, application_id: int):
    """Получение одной конкретной роли по id"""

    result = await session.execute(
        select(BootcampRoles).where(BootcampRoles.id == application_id)
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
            BootcampRoles.id.label("application_id"),
        )
        .join(User)
        .where(
            and_(
                BootcampRoles.role.in_(["участник", "админ"]),
                BootcampRoles.bootcamp_id == bootcamp_id,
                User.id == BootcampRoles.user_id,
            )
        )
        .where(not_(BootcampRoles.role.in_(["отклонено", "ожидание"])))
    )

    # Преобразуем результат в список словарей
    rows = result.all()
    keys = result.keys()
    return [dict(zip(keys, row)) for row in rows]

