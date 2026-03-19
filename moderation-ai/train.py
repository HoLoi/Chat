import logging
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report
from sklearn.pipeline import Pipeline

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)

BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"
SAVED_MODEL_DIR = BASE_DIR / "saved_model"

TEXT_COLUMN = "free_text"
LABEL_COLUMN = "label_id"


def _load_csv(file_name: str) -> pd.DataFrame:
    file_path = DATA_DIR / file_name
    if not file_path.exists():
        raise FileNotFoundError(f"Missing dataset file: {file_path}")

    df = pd.read_csv(file_path)
    if TEXT_COLUMN not in df.columns or LABEL_COLUMN not in df.columns:
        raise ValueError(
            f"{file_path.name} must contain columns: {TEXT_COLUMN}, {LABEL_COLUMN}"
        )

    # Keep only required columns, clean null/empty text rows, and coerce labels to int.
    df = df[[TEXT_COLUMN, LABEL_COLUMN]].dropna(subset=[TEXT_COLUMN, LABEL_COLUMN]).copy()
    df[TEXT_COLUMN] = df[TEXT_COLUMN].astype(str).str.strip()
    df = df[df[TEXT_COLUMN] != ""]

    numeric_label = pd.to_numeric(df[LABEL_COLUMN], errors="coerce")
    df = df[numeric_label.notna()].copy()
    df[LABEL_COLUMN] = numeric_label[numeric_label.notna()].astype(int)

    # Map multi-class toxicity labels into current binary API contract:
    # 0 -> clean, 1/2/... -> toxic.
    df["label"] = (df[LABEL_COLUMN] > 0).astype(int)
    df = df.rename(columns={TEXT_COLUMN: "text"})

    return df[["text", "label"]]


def load_dataset_splits() -> tuple[pd.DataFrame, pd.DataFrame]:
    train_df = _load_csv("train.csv")
    dev_df = _load_csv("dev.csv")
    test_df = _load_csv("test.csv")

    train_merged = pd.concat([train_df, dev_df], ignore_index=True)

    logger.info("Loaded train split: %d rows", len(train_df))
    logger.info("Loaded dev split: %d rows", len(dev_df))
    logger.info("Loaded test split: %d rows", len(test_df))
    logger.info("Merged train+dev size: %d rows", len(train_merged))

    return train_merged, test_df


def train_and_save() -> None:
    train_df, test_df = load_dataset_splits()

    X_train = train_df["text"]
    y_train = train_df["label"]
    X_test = test_df["text"]
    y_test = test_df["label"]

    pipeline = Pipeline(
        [
            ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1)),
            ("clf", LogisticRegression(max_iter=500, n_jobs=1, class_weight="balanced")),
        ]
    )

    pipeline.fit(X_train, y_train)
    logger.info("Model trained on CSV datasets")

    y_pred = pipeline.predict(X_test)
    report = classification_report(y_test, y_pred, target_names=["clean", "toxic"])
    logger.info("\n%s", report)

    SAVED_MODEL_DIR.mkdir(parents=True, exist_ok=True)
    vectorizer: TfidfVectorizer = pipeline.named_steps["tfidf"]
    classifier: LogisticRegression = pipeline.named_steps["clf"]

    joblib.dump(vectorizer, SAVED_MODEL_DIR / "vectorizer.pkl")
    joblib.dump(classifier, SAVED_MODEL_DIR / "model.pkl")
    logger.info("Artifacts saved to %s", SAVED_MODEL_DIR)


if __name__ == "__main__":
    train_and_save()