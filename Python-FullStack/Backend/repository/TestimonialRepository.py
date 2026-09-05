from app.repository.base import BaseRepository
from app.main import Testimonial
class TestimonialRepository(BaseRepository):
    def __init__(self, db): super().__init__(db,Testimonial)
