from fastapi import FastAPI, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi import FastAPI, WebSocket

import src

app = FastAPI(debug=True)

origins = ["http://localhost", "http://localhost:8080", "http://localhost:3000", "*"]
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def db_init_models():
    """Initial db models"""
    await src.init_models()
    print("Done")


app.include_router(src.users_router, prefix="/api/users", tags=["Users"])

app.include_router(src.chats_router, prefix="/api/chats", tags=["Chats"])

app.include_router(src.messages_router, prefix="/api/messages", tags=["Messages"])

app.include_router(src.bootcamps_router, prefix="/api/bootcamps", tags=["Bootcamps"])

app.include_router(src.websocket_router, prefix="/api/ws", tags=["WS_Messages"])



