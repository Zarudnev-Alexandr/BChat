import datetime
from pydantic import BaseModel


class BootcampCreate(BaseModel):
    address: str
    start_time: datetime.datetime
    end_time: datetime.datetime
    budget: int
    members_count: int
    description: str


class BootcampBase(BootcampCreate):
    id: int

class BootcampFull(BootcampBase):
    geoposition_longitude: float
    geoposition_latitude: float

