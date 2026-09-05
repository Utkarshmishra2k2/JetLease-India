from app.repository.base import BaseRepository
from app.main import Report
class ReportRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Report)
