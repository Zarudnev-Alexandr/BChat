from fastapi import APIRouter, Depends, HTTPException, Query
from enum import Enum
from geopy.distance import geodesic
from typing import List

from src.db import BootcampRolesEnum

from src.schemas import BootcampCreate, BootcampBase, BootcampFull

from src.utils import (
    get_current_user,
    get_bootcamps,
    get_bootcamps_admin,
    add_bootcamp,
    add_bootcamp_role,
    get_bootcamp,
    check_bootcamp_membership,
    remove_bootcamp,
    remove_bootcamp_role,
    get_bootcamp_applications,
    get_bootcamp_application_by_id,
    get_bootcamp_members,
    get_bootcamps_member,
    leave_bootcamp,
)


bootcamps_router: APIRouter = APIRouter()


@bootcamps_router.get("/", response_model=list[BootcampFull])
async def get_bootcamps_func(
    user_longitude: float,
    user_latitude: float,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    current_user: dict = Depends(get_current_user),
):
    """Все буткемпы"""

    offset = offset * limit

    bootcamps = await get_bootcamps(
        current_user.session, user_latitude, user_longitude, limit, offset
    )

    if not bootcamps:
        raise HTTPException(status_code=404, detail="Не найдено буткемпов😢")

    return bootcamps


@bootcamps_router.get("/admin", response_model=list[BootcampFull])
async def get_bootcamps_admin_func(
    user_longitude: float,
    user_latitude: float,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    current_user: dict = Depends(get_current_user),
):
    """Все буткемпы, где пользователь админ"""

    offset = offset * limit

    bootcamps = await get_bootcamps_admin(
        current_user.session,
        current_user.id,
        user_latitude,
        user_longitude,
        limit,
        offset,
    )

    if not bootcamps:
        raise HTTPException(status_code=404, detail=f"Не найдено буткемпов😢")

    return bootcamps


@bootcamps_router.get("/member", response_model=list[BootcampFull])
async def get_bootcamps_member_func(
    user_longitude: float,
    user_latitude: float,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    current_user: dict = Depends(get_current_user),
):
    """Все буткемпы, где пользователь участник"""

    offset = offset * limit

    bootcamps = await get_bootcamps_member(
        current_user.session,
        current_user.id,
        user_longitude,
        user_latitude,
        limit,
        offset,
    )

    if not bootcamps:
        raise HTTPException(status_code=404, detail=f"Не найдено буткемпов😢")

    return bootcamps


@bootcamps_router.post("/")
async def add_bootcamps_func(
    bootcamp: BootcampCreate, current_user: dict = Depends(get_current_user)
):
    """Добавление буткемпа"""

    bootcamp = {
        "address": bootcamp.address,
        "visible_address": bootcamp.visible_address,
        "start_time": bootcamp.start_time,
        "end_time": bootcamp.end_time,
        "budget": bootcamp.budget,
        "members_count": bootcamp.members_count,
        "description": bootcamp.description,
    }

    try:
        bootcampAdd = await add_bootcamp(current_user.session, **bootcamp)
        if bootcampAdd:
            bootcamp_role = {
                "role": BootcampRolesEnum.admin,
                "text": "создано админом",
                "bootcamp_id": bootcampAdd.id,
                "user_id": current_user.id,
            }
            role = await add_bootcamp_role(current_user.session, **bootcamp_role)

            result = [bootcampAdd, role]
            return result
    except Exception as e:
        print(e)
        await current_user.session.rollback()


@bootcamps_router.delete("/leave/{bootcamp_id}/")
async def leave_bootcamp_func(
    bootcamp_id: int, current_user: dict = Depends(get_current_user)
):
    """Пользователь покидвет буткемп"""

    bootcamp = await get_bootcamp(current_user.session, bootcamp_id)
    bootcamp_member = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )
    bootcamp_member_is_admin = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )

    if not bootcamp:
        raise HTTPException(
            status_code=404, detail=f"Не найдено буткемпа с id {bootcamp_id}"
        )

    if not bootcamp_member:
        raise HTTPException(
            status_code=403, detail=f"Вы не являетесь участником буткемпа🥱"
        )

    if bootcamp_member_is_admin.role == BootcampRolesEnum.admin:
        raise HTTPException(
            status_code=403,
            detail=f"Вы ходите покинуть свой же буткемп?🤨 Используйте удаление",
        )

    try:
        result = await leave_bootcamp(
            current_user.session, current_user.id, bootcamp_id
        )
        if result:
            return {"message": f"Вы успешно покинули буткемп с id {bootcamp_id}"}
    except:
        raise HTTPException(status_code=400, detail=f"Буткемп покинуть невозможно😈")


@bootcamps_router.delete("/{bootcamp_id}/")
async def delete_bootcamp_func(
    bootcamp_id: int, current_user: dict = Depends(get_current_user)
):
    """Удаление буткемпа"""

    bootcamp = await get_bootcamp(current_user.session, bootcamp_id)
    bootcamp_member_is_admin = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )

    if not bootcamp:
        raise HTTPException(
            status_code=404, detail=f"Не найдено буткемпа с id {bootcamp_id}"
        )

    if not bootcamp_member_is_admin:
        raise HTTPException(
            status_code=400, detail=f"Вы не имеете к этому буткемпу никакого отношения"
        )

    if bootcamp_member_is_admin.role != BootcampRolesEnum.admin:
        raise HTTPException(
            status_code=403,
            detail=f"Пользователь с id {current_user.id} не является админом буткемпа с id {bootcamp_id}",
        )

    try:
        await remove_bootcamp_role(current_user.session, bootcamp_id)
        result = await remove_bootcamp(current_user.session, bootcamp_id)
        if result:
            return {
                "message": f"Буткемп с id {bootcamp_id} успешно удален его создателем с id {current_user.id}"
            }
    except:
        raise HTTPException(status_code=404, detail=f"Не получилось удалить буткемп😢")


@bootcamps_router.post("/{bootcamp_id}/apply/")
async def add_bootcamp_apply_func(
    bootcamp_id: int, text: str, current_user: dict = Depends(get_current_user)
):
    """Отправка заявки на буткемп"""

    bootcamp = await get_bootcamp(current_user.session, bootcamp_id)
    bootcamp_member = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )

    bootcamp_members_count = await get_bootcamp_members(current_user.session, bootcamp_id)

    if not bootcamp:
        raise HTTPException(
            status_code=404, detail=f"Буткемпа с id {bootcamp_id} не существует😢"
        )
    
    

    if bootcamp_member:
        if bootcamp_member.role == BootcampRolesEnum.admin:
            raise HTTPException(
                status_code=400,
                detail=f"Вы уже являетесь админом этого буткемпа😎",
            )

        if bootcamp_member.role == BootcampRolesEnum.member:
            raise HTTPException(
                status_code=400,
                detail=f"Вы уже являетесь участником этого буткемпа🔥",
            )

        if bootcamp_member.role == BootcampRolesEnum.wating:
            raise HTTPException(
                status_code=400,
                detail=f"Ваша заявка на участие в буткемпе на рассмотрении⌛",
            )

        if bootcamp_member.role == BootcampRolesEnum.rejected:
            raise HTTPException(
                status_code=400,
                detail=f"Ваша заявка на участие в буткемпе отклонена😭",
            )       
        
    if len(bootcamp_members_count) >= bootcamp.members_count:
        raise HTTPException(
              status_code=403,
              detail=f"Буткемп уже переполнен, там жара⚡",
          )

    bootcamp_role = {
        "role": BootcampRolesEnum.wating,
        "text": text,
        "bootcamp_id": bootcamp_id,
        "user_id": current_user.id,
    }
    role = await add_bootcamp_role(current_user.session, **bootcamp_role)
    return role


@bootcamps_router.get("/{bootcamp_id}/applications/")
async def get_bootcamp_applications_func(
    bootcamp_id: int, current_user: dict = Depends(get_current_user)
):
    """Просмотр заявок на буткемп"""

    bootcamp = await get_bootcamp(current_user.session, bootcamp_id)
    bootcamp_member_is_admin = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )
    bootcamp_applications = await get_bootcamp_applications(
        current_user.session, bootcamp_id
    )

    if not bootcamp:
        raise HTTPException(
            status_code=404, detail=f"Такого буткемпа не существует❌"
        )

    if not bootcamp_member_is_admin:
        raise HTTPException(
            status_code=400, detail=f"Вы не имеете к этому буткемпу никакого отношения🤡"
        )

    if bootcamp_member_is_admin.role != BootcampRolesEnum.admin:
        raise HTTPException(
            status_code=403,
            detail=f"Вы не являетесь админом этого буткемпа💀",
        )

    if not bootcamp_applications:
        raise HTTPException(
            status_code=404,
            detail=f"Не найдено активных заявок на этот буткемп🙄",
        )

    return bootcamp_applications


class BootcampChangeRolesEnum(str, Enum):
    member = "участник"
    rejected = "отклонено"


@bootcamps_router.put("/{bootcamp_id}/applications/{application_id}/")
async def edit_bootcamp_applications_func(
    bootcamp_id: int,
    application_id: int,
    status: BootcampChangeRolesEnum,
    current_user: dict = Depends(get_current_user),
):
    """Изменение админом статуса заявки (одобрить/отклонить)"""

    bootcamp = await get_bootcamp(current_user.session, bootcamp_id)
    bootcamp_member_is_admin = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )
    bootcamp_application = await get_bootcamp_application_by_id(
        current_user.session, application_id
    )

    if not bootcamp:
        raise HTTPException(
            status_code=404, detail=f"Такого буткемпа не существует❌"
        )

    if not bootcamp_member_is_admin:
        raise HTTPException(
            status_code=400, detail=f"Вы не имеете к этому буткемпу никакого отношения"
        )

    if bootcamp_member_is_admin.role != BootcampRolesEnum.admin:
        raise HTTPException(
            status_code=403,
            detail=f"Вы не являетесь админом этого буткемпа💀",
        )

    if not bootcamp_application:
        raise HTTPException(
            status_code=404, detail=f"Не найдено заявки с id {application_id}😱"
        )

    if bootcamp_application.user_id == current_user.id:
        raise HTTPException(
            status_code=400, detail=f"Вы не можете изменить свою роль, вы же админ😕"
        )

    try:
        bootcamp_application.role = status
        await current_user.session.commit()
        return bootcamp_application

    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Ошибка при обработке запроса: {str(e)}"
        )


@bootcamps_router.get("/{bootcamp_id}/members/")
async def get_bootcamp_members_func(
    bootcamp_id: int, current_user: dict = Depends(get_current_user)
):
    """Получение всех участников буткемпа"""

    bootcamp = await get_bootcamp(current_user.session, bootcamp_id)
    bootcamp_status = await check_bootcamp_membership(
        current_user.session, current_user.id, bootcamp_id
    )
    bootcamp_members = await get_bootcamp_members(current_user.session, bootcamp_id)

    if not bootcamp:
        raise HTTPException(
            status_code=404, detail=f"Этого буткемпа не существует❌"
        )

    if not bootcamp_status:
        raise HTTPException(
            status_code=400,
            detail=f"Вы не подавали заявку на участие в этом буткемпе💔",
        )

    if bootcamp_status.role == BootcampRolesEnum.wating:
        raise HTTPException(
            status_code=403,
            detail=f"Вы не можете просматривать участников буткемпа, ваша заявка все еще на стадии ожидания⏳",
        )

    if bootcamp_status.role == BootcampRolesEnum.rejected:
        raise HTTPException(
            status_code=403,
            detail=f"Вы не можете просматривать участников буткемпа, ваша заявка отклонена😈",
        )

    return bootcamp_members
