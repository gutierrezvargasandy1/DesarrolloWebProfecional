from dataclasses import dataclass
from typing import TypeVar, Generic, Optional
from flask import jsonify

T = TypeVar("T")

@dataclass
class ApiResponse(Generic[T]):
    status: int
    message: str
    data: Optional[T] = None

    def to_response(self):
        success = self.status == 200 or self.status == 201
        return jsonify({
            "success": success,
            "status": self.status,
            "message": self.message,
            "data": self.data
        }), self.status