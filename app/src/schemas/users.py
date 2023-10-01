import datetime
from typing import Optional
from pydantic import BaseModel

"""Возврат всех юзеров для скролла(только основная информация)"""


class AllUsersProfilesMain(BaseModel):
    id: int
    nickname: str
    name: str
    surname: str
    imageURL: Optional[str] = None
    is_online: bool


"""Возврат всей информации об одном пользователе"""


class UserProfile(AllUsersProfilesMain):
    date_of_birth: datetime.datetime
    email: str
    hashed_password: str
    is_active: bool
    date_of_create: datetime.datetime


"""Создание пользователя при регистрации(только основная информация)"""


class UserCreate(BaseModel):
    nickname: str
    name: str
    surname: str
    date_of_birth: datetime.datetime
    email: str
    hashed_password: str
    imageURL: Optional[str]
