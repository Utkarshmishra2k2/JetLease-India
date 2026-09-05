from app.repository.base import BaseRepository
from app.main import ContactMessage
class ContactMessageRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,ContactMessage)
