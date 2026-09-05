from app.repository.base import BaseRepository
from app.main import Route
class RouteRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Route)
