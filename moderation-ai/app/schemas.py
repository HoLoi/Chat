from pydantic import BaseModel, Field


class ModerateRequest(BaseModel):
    text: str = Field(..., min_length=1)
    userId: int = Field(..., ge=0)
    roomId: int = Field(..., ge=0)


class ModerateResponse(BaseModel):
    label: str
    score: float
    action: str
