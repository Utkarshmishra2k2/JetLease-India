from app.repository.base import BaseRepository
from app.main import Crew
class CrewRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Crew)
