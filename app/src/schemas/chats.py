import datetime
from pydantic import BaseModel


"""Создание чата"""


class ChatCreate(BaseModel):
    name: str
    id_creator: int


"""Добавление пользователя в чат"""


class ChatUserCreate(BaseModel):
    chat_id: int
    user_id: int


"""Вся инфа о чате"""


class ChatBase(ChatCreate):
    id: int
    date_of_create: datetime.datetime


"""Вся информация об участнике чата"""


class ChatUser(ChatUserCreate):
    id: int
    date_of_join: datetime.datetime
