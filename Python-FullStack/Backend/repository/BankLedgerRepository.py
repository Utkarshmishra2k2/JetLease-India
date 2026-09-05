from app.repository.base import BaseRepository
from app.main import BankLedger
class BankLedgerRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,BankLedger)
