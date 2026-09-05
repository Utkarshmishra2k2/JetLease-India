from app.repository.base import BaseRepository
from app.main import AadhaarRegistry
class AadhaarRegistryRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,AadhaarRegistry)
