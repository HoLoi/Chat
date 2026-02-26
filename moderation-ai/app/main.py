import logging
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse

from .schemas import ModerateRequest, ModerateResponse
from .utils import ModelRegistry, map_action

# cach chay server AI
# .venv\Scripts\activate
# python -m uvicorn app.main:app --reload --port 5000

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)

app = FastAPI(title="Moderation AI", version="1.0.0")
model_registry = ModelRegistry()


@app.on_event("startup")
async def startup_event() -> None:
    model_dir = Path(__file__).resolve().parent.parent / "saved_model"
    try:
        model_registry.load(model_dir)
    except FileNotFoundError:
        logger.warning("Model artifacts missing; run train.py to create them")
    except Exception as exc:  # noqa: BLE001
        logger.exception("Failed to load model on startup: %s", exc)


@app.post("/moderate", response_model=ModerateResponse)
async def moderate(payload: ModerateRequest) -> JSONResponse:
    if not payload.text.strip():
        raise HTTPException(status_code=400, detail="text must not be empty")

    label, score = model_registry.predict_safe(payload.text)
    action = map_action(score)

    response = ModerateResponse(label=label, score=round(score, 4), action=action)
    return JSONResponse(status_code=200, content=response.model_dump())


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}
