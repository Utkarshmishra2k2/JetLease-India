from app.repository.base import BaseRepository
from app.main import PilotLicenseRegistry
class PilotLicenseRegistryRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,PilotLicenseRegistry)
