from app.main import require_auth
class AuthContext:
    requireAuth=staticmethod(require_auth)
    requireCustomer=staticmethod(lambda request, db: require_auth(request, db, "customer"))
    requireAdmin=staticmethod(lambda request, db: require_auth(request, db, "admin"))
