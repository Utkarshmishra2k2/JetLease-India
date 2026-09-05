from app.repository.base import BaseRepository
from app.main import Notification
class NotificationRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Notification)
