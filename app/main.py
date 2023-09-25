from fastapi import FastAPI

import src

app = FastAPI()


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