from app.repository.base import BaseRepository
from app.main import Faq
class FaqRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Faq)
