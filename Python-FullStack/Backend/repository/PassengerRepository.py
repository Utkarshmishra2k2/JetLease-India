from app.repository.base import BaseRepository
from app.main import Passenger
class PassengerRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Passenger)
