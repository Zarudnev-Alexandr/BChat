from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from models import * 
from database import SessionLocal, engine

app = FastAPI()

Base.metadata.create_all(bind=engine)