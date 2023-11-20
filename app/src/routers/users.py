from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, Form, status
from fastapi.responses import JSONResponse
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi.responses import StreamingResponse
from sqlalchemy import select
from src.db import get_session

# from src.db import get_password_hash
import os
from pathlib import Path
from dotenv import load_dotenv

# from app.src.utils.security import get_current_user

from src.schemas import (
    AllUsersProfilesMain,
    UserCreate,
    UserProfile,
)
from src.utils import (
    add_user,
    get_user,
    # get_user_chats,
    get_users,
    authenticate_user,
    update_avatar_user,
    create_access_token,
    get_current_user,
    get_password_hash,
    get_user_by_nickname,
)

cur_path = Path(__file__).resolve()
load_dotenv(cur_path.parent.parent / ".env")

AVATARPATH = os.getenv("AVATARPATH")

users_router: APIRouter = APIRouter()


from fastapi import Form


@users_router.post("/login")
async def login(
    email: str = Form(...),
    password: str = Form(...),
    session: AsyncSession = Depends(get_session),
):
    """Логин пользователя"""
    user = await authenticate_user(session, email, password)
    if user:
        access_token = await create_access_token(data={"email": user.email})
        return JSONResponse(
            content={
                "access_token": access_token,
                "token_type": "bearer",
                "user_id": str(user.id),
            },
            status_code=status.HTTP_200_OK,
        )
    else:
        return JSONResponse(
            content={"message": "Неверный email или пароль"},
            status_code=status.HTTP_401_UNAUTHORIZED,
        )


@users_router.get("/", response_model=list[AllUsersProfilesMain])
async def get_all_users(current_user: dict = Depends(get_current_user)):
    """Список всех пользователей, удобен для скролла, мало данных"""
    users = await get_users(current_user.session)
    return users


@users_router.get("/{id}", response_model=UserProfile)
async def get_one_user(id: int, current_user: dict = Depends(get_current_user)):
    """Вся информация об одном пользователе по его id"""
    user = await get_user(current_user.session, id)
    return (
        user
        if user
        else JSONResponse(
            content={"message": "Такого пользователя не существует"}, status_code=404
        )
    )


@users_router.get("/{nickname}/", response_model=UserProfile)
async def get_one_user_by_nickname(
    nickname: str, current_user: dict = Depends(get_current_user)
):
    """Вся информация об одном пользователе по его никнецму"""
    user = await get_user_by_nickname(current_user.session, nickname)

    if not user:
        raise HTTPException(
            status_code=404, detail=f"Пользователь с таким никнеймом не обнаружен😭"
        )

    if user.id == current_user.id:
        raise HTTPException(
            status_code=400,
            detail=f"Поздравляем, вы нашли самого себя😱, а теперь закройте это окно и по нормальному",
        )

    return user


@users_router.get("/profile/", response_model=UserProfile)
async def get_me(current_user: dict = Depends(get_current_user)):
    """Получить мой профиль пользователя"""
    user = await get_user(current_user.session, current_user.id)
    if not user:
        return JSONResponse(
            {"message": "Невозможно получить профиль пользователя"}, status_code=500
        )
    return user


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
    image_file: UploadFile = File(...),
    current_user: dict = Depends(get_current_user),
):
    user = await get_user(current_user.session, current_user.id)
    if user:
        file_path = (
            AVATARPATH + str(current_user.id) + "." + image_file.filename.split(".")[-1]
        )
        with open(file_path, "wb") as f:
            f.write(image_file.file.read())
        await update_avatar_user(current_user.session, current_user.id, file_path)
        return JSONResponse({"message": f"Фото {file_path} загружено"})
    else:
        return JSONResponse(
            {"message": f"Не найден пользователь с id {current_user.id}"},
            status_code=404,
        )
