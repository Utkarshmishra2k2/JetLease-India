from app.repository.base import BaseRepository
from app.main import AuditLog
class AuditLogRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,AuditLog)
