from app.repository.base import BaseRepository
from app.main import Booking
class BookingRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Booking)
