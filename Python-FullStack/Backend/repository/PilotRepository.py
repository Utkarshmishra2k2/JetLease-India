from app.repository.base import BaseRepository
from app.main import Pilot
class PilotRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Pilot)
