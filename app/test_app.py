import asyncio

import pytest
import pytest_asyncio
from httpx import AsyncClient
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker

from src.db import get_session as get_db, SQLALCHEMY_DATABASE_URL
from src.db import Base
from main import app
from src.schemas import UserCreate
from src.utils import add_user
from src.db import get_password_hash


@pytest.fixture(scope="session")
def event_loop() -> asyncio.AbstractEventLoop:
    loop = asyncio.get_event_loop_policy().new_event_loop()
    yield loop
    loop.close()


@pytest.fixture(scope="session")
def engine():
    engine = create_async_engine(
        "postgresql+asyncpg://postgres:test@localhost:5432/test"
    )
    yield engine
    engine.sync_engine.dispose()


@pytest_asyncio.fixture(scope="session")
async def prepare_db():
    create_db_engine = create_async_engine(
        SQLALCHEMY_DATABASE_URL,
        isolation_level="AUTOCOMMIT",
    )
    async with create_db_engine.begin() as connection:
        await connection.execute(
            text("drop database if exists {name};".format(name="test")),
        )
        await connection.execute(
            text("create database {name};".format(name="test")),
        )


@pytest_asyncio.fixture(scope="session")
async def db_session(engine) -> AsyncSession:
    async with engine.begin() as connection:
        await connection.run_sync(Base.metadata.drop_all)
        await connection.run_sync(Base.metadata.create_all)
        TestingSessionLocal = sessionmaker(
            expire_on_commit=False,
            class_=AsyncSession,
            bind=engine,
        )
        async with TestingSessionLocal(bind=connection) as session:
            yield session
            await session.flush()
            await session.rollback()


@pytest.fixture(scope="session")
def override_get_db(prepare_db, db_session: AsyncSession):
    async def _override_get_db():
        yield db_session

    return _override_get_db


@pytest_asyncio.fixture(scope="session")
async def async_client(override_get_db):
    app.dependency_overrides[get_db] = override_get_db
    async with AsyncClient(app=app, base_url="http://test") as ac:
        yield ac


@pytest_asyncio.fixture
async def test_user(user: UserCreate, session: AsyncSession):
    user = {
        "nickname": "pytestNickname",
        "name": "pytestName",
        "surname": "pytestSurname",
        "date_of_birth": "pytestDate",
        "email": "pytestEmain",
        "hashed_password": get_password_hash("1234"),
        "imageURL": "pytestUrl",
    }
    user_db = await add_user(session, **user)
    yield user_db
    await db_session.delete(user_db)
    await db_session.commit()


@pytest.mark.asyncio
async def test_sign_up(async_client, db_session):
    request_data = {"email": "pytestEmain", "password": "1234"}
    response = await async_client.post("/sign-up/", json=request_data)
    token_counts = await db_session.execute(select(func.count(UserToken.id)))
    assert token_counts.scalar_one() == 1
    assert response.status_code == 200
    assert response.json()["id"] is not None
    assert response.json()["email"] == "sam_vimes@citywatch.com"
    assert response.json()["name"] == "Sam Vimes"
    assert response.json()["token"]["access_token"] is not None
    assert response.json()["token"]["expires"] is not None
    assert response.json()["token"]["token_type"] == "bearer"
