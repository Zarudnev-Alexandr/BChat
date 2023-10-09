import datetime
from pydantic import BaseModel

"""Возврат сообщения"""


class CreateMessageSchema(BaseModel):
    text: str
    sender_id: int
    chat_id: int


class MessageSchema(CreateMessageSchema):
    id: int
    date_of_create: datetime.datetime
