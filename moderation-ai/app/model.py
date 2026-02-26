import logging
from typing import Optional, Tuple

import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression

logger = logging.getLogger(__name__)


class ToxicityModel:
    def __init__(self, vectorizer: TfidfVectorizer, classifier: LogisticRegression):
        self.vectorizer = vectorizer
        self.classifier = classifier

    @classmethod
    def load(cls, vectorizer_path: str, model_path: str) -> "ToxicityModel":
        try:
            vectorizer: TfidfVectorizer = joblib.load(vectorizer_path)
            classifier: LogisticRegression = joblib.load(model_path)
            logger.info("Model and vectorizer loaded successfully")
            return cls(vectorizer, classifier)
        except Exception as exc:  # noqa: BLE001
            logger.exception("Failed to load model artifacts: %s", exc)
            raise

    def predict(self, text: str) -> Tuple[str, float]:
        probabilities = self.classifier.predict_proba(self.vectorizer.transform([text]))[0]
        # Assuming binary classes in order [clean, toxic]
        toxic_score = float(probabilities[1])
        label = "toxic" if toxic_score >= 0.5 else "clean"
        return label, toxic_score
