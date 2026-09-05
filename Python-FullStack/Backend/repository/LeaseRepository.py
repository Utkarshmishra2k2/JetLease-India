from app.repository.base import BaseRepository
from app.main import Lease
class LeaseRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Lease)
