from flask import Flask, request, jsonify

app = Flask(__name__)

# Very simple heuristic stub: returns block for some bad words, warn for maybe, else allow
BLACKLIST = ["chửi", "dm", "vcl", "địt", "fuck"]
WARNLIST = ["ngu", "đần", "spam", "nhạy cảm"]

@app.route("/moderate", methods=["POST"])
def moderate():
    data = request.get_json(silent=True) or {}
    text = (data.get("text") or "").lower()

    for w in BLACKLIST:
        if w in text:
            return jsonify({
                "action": "block",
                "label": "abuse",
                "score": 0.95,
                "message": "Tin nhắn vi phạm tiêu chuẩn"
            })

    for w in WARNLIST:
        if w in text:
            return jsonify({
                "action": "warn",
                "label": "risk",
                "score": 0.45,
                "message": "Nội dung có thể không phù hợp. Bạn có muốn gửi?"
            })

    return jsonify({
        "action": "allow",
        "label": "clean",
        "score": 0.05,
        "message": None
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
