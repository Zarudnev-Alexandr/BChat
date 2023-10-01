from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

import src

app = FastAPI()

origins = [
    "http://localhost",
    "http://localhost:8080",
    "http://localhost:3000",
    "*"
]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)


@app.on_event("startup")
async def db_init_models():
    """Initial db models"""
    await src.init_models()
    print("Done")


app.include_router(
    src.users_router,
    prefix="/api/users",
    tags=["Users"]
)

app.include_router(
    src.chats_router,
    prefix="/api/chats",
    tags=["Chats"]
)

app.include_router(
    src.messages_router,
    prefix="/api/messages",
    tags=["Messages"]
)