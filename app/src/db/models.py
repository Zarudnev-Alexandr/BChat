from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, LargeBinary
from sqlalchemy.orm import relationship
from sqlalchemy.types import TIMESTAMP
import datetime
import uuid
from .database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    nickname = Column(String, unique=True, index=True, nullable=False)
    name = Column(String, nullable=False)
    surname = Column(String, nullable=False)
    date_of_birth = Column(TIMESTAMP(timezone=True), nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=False)
    is_online = Column(Boolean, default=False, index=True)
    imageURL = Column(String)
    date_of_create = Column(TIMESTAMP(timezone=True), default=datetime.datetime.now())

    chats = relationship("ChatUser", back_populates="user")
    messages = relationship("Message", back_populates="sender")


class Chat(Base):
    __tablename__ = "chats"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    date_of_create = Column(TIMESTAMP(timezone=True), default=datetime.datetime.now())
    # id_creator = Column(Integer, ForeignKey("users.id"))

    users = relationship("ChatUser", back_populates="chat")
    messages = relationship("Message", back_populates="chat")


class ChatUser(Base):
    __tablename__ = "chat_users"

    id = Column(Integer, primary_key=True, index=True)
    date_of_join = Column(TIMESTAMP(timezone=True), default=datetime.datetime.now())
    is_admin = Column(Boolean, unique=False, default=False)
    chat_id = Column(Integer, ForeignKey("chats.id"))
    user_id = Column(Integer, ForeignKey("users.id"))

    user = relationship("User", back_populates="chats")
    chat = relationship("Chat", back_populates="users")


class Message(Base):
    __tablename__ = "messages"

    id = Column(Integer, primary_key=True, index=True)
    text = Column(String)
    date_of_create = Column(TIMESTAMP(timezone=True), default=datetime.datetime.now())
    is_edit = Column(Boolean, unique=False, default=False)
    sender_id = Column(Integer, ForeignKey("users.id"))
    chat_id = Column(Integer, ForeignKey("chats.id"))

    sender = relationship("User", back_populates="messages")
    chat = relationship("Chat", back_populates="messages")
    files = relationship("File", back_populates="message")


class File(Base):
    __tablename__ = "files"

    id = Column(Integer, primary_key=True, index=True)
    filename = Column(String)
    content_type = Column(String)
    file_size = Column(Integer)
    message_id = Column(Integer, ForeignKey("messages.id"))
    date_of_create = Column(TIMESTAMP(timezone=True), default=datetime.datetime.now())

    message = relationship("Message", back_populates="files")
