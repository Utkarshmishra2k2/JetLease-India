from app.repository.base import BaseRepository
from app.main import Payment
class PaymentRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Payment)
