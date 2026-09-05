class ErrorResponse:
    def __init__(self,status,message,timestamp=None,fieldErrors=None): self.status=status; self.message=message; self.timestamp=timestamp; self.fieldErrors=fieldErrors
