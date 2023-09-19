from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, Float, DateTime
from sqlalchemy.orm import relationship

from database import Base


class User(Base):
  __tablename__ = "users"

  id = Column(Integer, primary_key=True, index=True)
  nickname = Column(String, unique=True, index=True)
  name = Column(String)
  surname = Column(String)
  date_of_birth = Column(DateTime)
  email = Column(String, unique=True, index=True)
  hashed_password = Column(String)
  is_active = Column(Boolean, default=False)
  imageURL = Column(String)

  chats = relationship("ChatUser", back_populates="user")
  messages = relationship("Message", back_populates="sender")


class Chat(Base):
  __tablename__ = "chats"

  id = Column(Integer, primary_key=True, index=True)
  name = Column(String)
  date_of_create = Column(DateTime)
  id_creator = Column(Integer, ForeignKey("users.id"))

  users = relationship("ChatUser", back_populates="chat")
  messages = relationship("Messages", back_populates="chat")


class ChatUser(Base):
  __tablename__ = "chat_users"

  id = Column(Integer, primary_key=True, index=True)
  date_of_join = Column(DateTime)
  chat_id = Column(Integer, ForeignKey("chats.id"))
  user_id = Column(Integer, ForeignKey("users.id"))

  user = relationship("User", back_populates="chats")
  chat = relationship("Chat", back_populates="users")


class Message(Base):
  __tablename__ = "messages"

  id = Column(Integer, primary_key=True, index=True)
  text = Column(String)
  timestamp = Column(DateTime)
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

    message = relationship("Message", back_populates="files")