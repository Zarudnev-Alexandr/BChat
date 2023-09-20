from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from fastapi.responses import JSONResponse
from db.database import init_models, get_session
from db.models import *  # Импортируем из models.py
from db.schemas import UserProfilesAll, UserCreate
from db.utils import get_users, add_user

app = FastAPI()

@app.on_event("startup")
async def db_init_models():
    """Initial db models"""
    await init_models()
    print("Done")

@app.get("/api/users")
async def get_all_users(session: AsyncSession = Depends(get_session)):
    """Список всех пользователей, удобен для скролла, мало данных"""

    users = await get_users(session)
    return [UserProfilesAll(id=u.id, nickname=u.nickname, name=u.name, surname=u.surname) for u in users]

@app.post("/api/users/add")
async def add_one_user(user:UserCreate, 
                       session:AsyncSession = Depends(get_session)):
    """Add one User to db"""
    userAdd = await add_user(session, user.nickname, user.name, user.surname)
    try:
        await session.commit()
        return userAdd
    except Exception as e:
        print(e)
        await session.rollback()
        return JSONResponse(content={"message": "User not added"}, status_code=500)