from app.main import sessions
import uuid
class SessionService:
    def createSession(self,email,role):
        t=uuid.uuid4().hex; sessions[t]={"email":email,"role":role}; return t
    def getSession(self,token): return sessions.get(token)
    def invalidate(self,token): sessions.pop(token,None)
