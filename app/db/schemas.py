import datetime
from typing import Optional
from pydantic import BaseModel

class UserProfilesAll(BaseModel):
  id: int
  nickname: str
  name: str
  surname: str
  # date_of_birth: datetime.datetime
  imageURL: Optional[str] = None

class UserCreate(BaseModel):
  nickname: str
  name: str
  surname: str




# from pydantic import BaseModel
# from typing import List
# from .models import *


# class FileBase(BaseModel):
#     filename: str
#     content_type: str
#     file_size: int


# class FileCreate(FileBase):
#     pass


# class File(FileBase):
#     id: int
#     date_of_create: datetime


# class UserBase(BaseModel):
#     nickname: str
#     name: str
#     surname: str
#     date_of_birth: datetime
#     email: str
#     is_active: bool
#     is_online: bool
#     imageURL: str


# class UserCreate(UserBase):
#     hashed_password: str


# class User(UserBase):
#     id: int
#     token: str
#     date_of_create: datetime
#     chats: List[Chat] = []
#     messages: List[Message] = []


# class ChatBase(BaseModel):
#     name: str


# class ChatCreate(ChatBase):
#     pass


# class Chat(ChatBase):
#     id: int
#     date_of_create: datetime
#     id_creator: int
#     users: List[User] = []
#     messages: List[Message] = []


# class ChatUserBase(BaseModel):
#     date_of_join: datetime


# class ChatUserCreate(ChatUserBase):
#     chat_id: int
#     user_id: int


# class ChatUser(ChatUserBase):
#     id: int
#     chat: Chat
#     user: User


# class MessageBase(BaseModel):
#     text: str


# class MessageCreate(MessageBase):
#     chat_id: int


# class Message(MessageBase):
#     id: int
#     date_of_create: datetime
#     sender: User
#     chat: Chat
#     files: List[File] = []