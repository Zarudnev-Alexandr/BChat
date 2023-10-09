import io
from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, Form
from fastapi.responses import JSONResponse
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi.responses import StreamingResponse
from sqlalchemy import select
from src.db import get_session
from src.db import get_password_hash, UserAvatar

from src.schemas import (
    AllUsersProfilesMain,
    UserCreate,
    UserProfile,
    UserAvatarCreate,
    UserAvatarResponse,
)
from src.utils import (
    add_user,
    get_user,
    get_user_chats,
    get_users,
    login_user,
    get_avatar,
    get_user_avatar
)

users_router: APIRouter = APIRouter()


@users_router.get("/login")
async def login(
    email: str, password: str, session: AsyncSession = Depends(get_session)
):
    """Логин пользователя"""
    userInfo = {"email": email, "password": password}
    user = await login_user(session, **userInfo)
    if user:
        return user
    else:
        return JSONResponse(
            content={"message": f"Не найдено email - {email} password - {password}"}
        )


@users_router.get("/", response_model=list[AllUsersProfilesMain])
async def get_all_users(session: AsyncSession = Depends(get_session)):
    """Список всех пользователей, удобен для скролла, мало данных"""
    users = await get_users(session)
    return users


@users_router.get("/{id}", response_model=UserProfile)
async def get_one_user(id: int, session: AsyncSession = Depends(get_session)):
    """Вся информация об одном пользователе по его id"""
    user = await get_user(session, id)
    user_avater = None
    return (
        user
        if user
        else JSONResponse(
            content={"message": "Такого пользователя не существует"}, status_code=404
        )
    )


@users_router.post("/register")
async def add_one_user(user: UserCreate, session: AsyncSession = Depends(get_session)):
    """Регистрация пользователя"""
    user = {
        "nickname": user.nickname,
        "name": user.name,
        "surname": user.surname,
        "date_of_birth": user.date_of_birth,
        "email": user.email,
        "hashed_password": get_password_hash(user.hashed_password),
    }
    userAdd = await add_user(session, **user)
    try:
        await session.commit()
        return userAdd
    except Exception as e:
        print(e.__dict__["orig"])
        await session.rollback()
        return JSONResponse(content={"message": "User not added"}, status_code=500)


@users_router.post("/user-avatar/")
async def create_file(
    user_id: int = Form(...),
    image_file: UploadFile = File(...),
    session: AsyncSession = Depends(get_session),
):
    content = await image_file.read()

    user = await get_user(session, user_id)
    if user:
        avatar = UserAvatar(
            filename=image_file.filename, content=content, id_user=user_id
        )
        session.add(avatar)
        await session.commit()
        return JSONResponse(
            content={
                "message": f"Аватар пользователя {user.nickname} с id {user.id} успешно загружен"
            },
            status_code=201,
        )
    else:
        return JSONResponse(
            content={"message": f"Пользователь с id {user_id} не найден"},
            status_code=404,
        )


@users_router.get("/avatar/{file_id}")
async def get_file(file_id: int, session: AsyncSession = Depends(get_session)):
    user_avatar = await get_avatar(session, file_id)
    if user_avatar is None:
        return JSONResponse(
            content={"message": f"Не удалось найти файл с id {file_id}"},
            status_code=404,
        )

    return StreamingResponse(
        io.BytesIO(user_avatar.content),
        media_type="application/octet-stream",
        headers={"Content-Disposition": f"attachment; filename={user_avatar.filename}"},
    )

@users_router.get("/user-avatar/{user_id}")
async def get_one_avatar_from_user(user_id: int, session: AsyncSession = Depends(get_session)):
    user_avatar = await get_user_avatar(session, user_id)
    if user_avatar:
        return StreamingResponse(
            io.BytesIO(user_avatar.content),
            media_type="application/octet-stream",
            headers={"Content-Disposition": f"attachment; filename={user_avatar.filename}"},
        )
    else:
        return JSONResponse({"message": f"Пользователь с id {user_id} без аватарки"}, status_code=404)


@users_router.get("/{id}/chats")
async def get_all_user_chats(id: int, session: AsyncSession = Depends(get_session)):
    """Все чаты конкретного пользователя"""
    user = await get_user(session, id)
    if user:
        user_chats = await get_user_chats(session, id)
        return user_chats
    else:
        return JSONResponse(
            content={"message": f"Не найден пользователь с id {id}"}, status_code=404
        )
