import logging
from pathlib import Path
from typing import Optional, Tuple

from .model import ToxicityModel

logger = logging.getLogger(__name__)


ACTION_THRESHOLDS = {
    "warn": 0.60,
    "block": 0.85,
}


class ModelRegistry:
    def __init__(self) -> None:
        self.model: Optional[ToxicityModel] = None

    def load(self, model_dir: Path) -> None:
        vectorizer_path = model_dir / "vectorizer.pkl"
        model_path = model_dir / "model.pkl"
        if not vectorizer_path.exists() or not model_path.exists():
            raise FileNotFoundError("Model artifacts not found; run train.py first")
        self.model = ToxicityModel.load(str(vectorizer_path), str(model_path))

    def predict_safe(self, text: str) -> Tuple[str, float]:
        if not self.model:
            logger.warning("Model not loaded; returning clean fallback")
            return "clean", 0.0
        try:
            return self.model.predict(text)
        except Exception as exc:  # noqa: BLE001
            logger.exception("Prediction failed: %s", exc)
            return "clean", 0.0


def map_action(score: float) -> str:
    if score >= ACTION_THRESHOLDS["block"]:
        return "block"
    if score >= ACTION_THRESHOLDS["warn"]:
        return "warn"
    return "allow"
