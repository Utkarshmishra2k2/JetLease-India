from app.repository.base import BaseRepository
from app.main import User
class UserRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,User)
