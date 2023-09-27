from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.ext.asyncio import AsyncSession
from src.db import get_session
from src.db import get_password_hash

from src.schemas import AllUsersProfilesMain, UserCreate, UserProfile
from src.utils import add_user, get_user, get_user_chats, get_users, login_user

users_router: APIRouter = APIRouter()


@users_router.get('/login')
async def login (email: str, password: str, session: AsyncSession = Depends(get_session)):
    """Логин пользователя"""
    userInfo = {
      "email": email,
      "password": password
    }
    user = await login_user(session, **userInfo)
    if user:
      return user
    else:
      return JSONResponse(content={"message": f"Не найдено email - {email} password - {password}"})


@users_router.get("/", response_model=list[AllUsersProfilesMain])
async def get_all_users(session: AsyncSession = Depends(get_session)):
    """Список всех пользователей, удобен для скролла, мало данных"""
    users = await get_users(session)
    return users


@users_router.get("/{id}", response_model=UserProfile)
async def get_one_user(id: int, session: AsyncSession = Depends(get_session)):
    """Вся информация об одном пользователе по его id"""
    user = await get_user(session, id)
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
        "imageURL": user.imageURL,
    }
    userAdd = await add_user(session, **user)
    try:
        await session.commit()
        return userAdd
    except Exception as e:
        print(e.__dict__["orig"])
        await session.rollback()
        return JSONResponse(content={"message": "User not added"}, status_code=500)
    

@users_router.get("/{id}/chats")
async def get_all_user_chats(id: int, session: AsyncSession = Depends(get_session)):
    """Все чаты конкретного пользователя"""
    user = await get_user(session, id)
    if user:
        user_chats = await get_user_chats(session, id)
        return user_chats
    else:
        return JSONResponse(content={"message": f"Не найден пользователь с id {id}"}, status_code=404)