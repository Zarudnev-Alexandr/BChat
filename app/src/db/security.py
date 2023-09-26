from datetime import datetime, timedelta
from jose import jwt
from passlib.context import CryptContext
from fastapi import FastAPI, Depends, HTTPException, APIRouter

# Создаем экземпляр FastAPI
security_router: APIRouter = APIRouter()

# Настройки для JWT
SECRET_KEY = "your-secret-key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# Создаем объект для хэширования паролей
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Функция для хэширования пароля
def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

# Функция для создания JWT токена
def create_access_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# Пример использования:
@security_router.post("/token")
async def login_for_access_token(username: str, password: str):
    # Здесь вы должны проверить пароль пользователя (обычно из базы данных)
    # В этом примере, мы сравниваем пароль с хэшем пароля, который сохранен в переменной.
    # В реальном приложении, пароль следует хранить в безопасной хэшированной форме в базе данных.
    hashed_password = get_password_hash(password)
    if username == "testuser" and password == hashed_password:
        access_token = create_access_token(data={"sub": username})
        return {"access_token": access_token, "token_type": "bearer"}
    raise HTTPException(status_code=401, detail="Unauthorized")
