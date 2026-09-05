from app.repository.base import BaseRepository
from app.main import Aircraft
class AircraftRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Aircraft)
