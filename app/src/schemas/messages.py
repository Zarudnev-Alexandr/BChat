import datetime
from pydantic import BaseModel

"""Возврат сообщения"""


class CreateMessageSchema(BaseModel):
    chat_id: int
    text: str


class MessageSchema(CreateMessageSchema):
    id: int
    sender_id: int
    date_of_create: datetime.datetime
    is_edit: bool


class EditMessageSchema(BaseModel):
    text: str
