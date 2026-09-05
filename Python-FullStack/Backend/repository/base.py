from sqlalchemy import select, func, or_
class BaseRepository:
    def __init__(self, db, model): self.db=db; self.model=model
    def findAll(self): return self.db.scalars(select(self.model)).all()
    def findById(self, id): return self.db.get(self.model,id)
    def save(self, entity): self.db.add(entity); self.db.commit(); self.db.refresh(entity); return entity
    def deleteById(self,id):
        x=self.findById(id)
        if x is not None: self.db.delete(x); self.db.commit()
    def count(self): return self.db.query(self.model).count()
