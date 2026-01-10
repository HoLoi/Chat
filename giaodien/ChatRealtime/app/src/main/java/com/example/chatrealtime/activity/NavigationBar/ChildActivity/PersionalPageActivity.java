package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PersionalPageActivity extends AppCompatActivity {

    private TextView tvName;
    private ShapeableImageView imgAvatar;
    private Button btnKetban, btnNhanTin;
    private ImageView btnback;

    int friendId;
    int myId; // ID người dùng hiện tại từ session
    String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_persional_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.tv_ten_personal_page);
        imgAvatar = findViewById(R.id.imgAvatarFriend);
        btnKetban = findViewById(R.id.btn_ketban_personal_page);
        btnNhanTin = findViewById(R.id.btn_nhantin);
        btnback = findViewById(R.id.btn_back_personal_page);

        SessionManager session = new SessionManager(this);
        myId = session.getMaTaiKhoan(); // Lấy ID tài khoản đúng bảng TAIKHOAN

        friendId = getIntent().getIntExtra("friendId", -1);

        if (friendId != -1) {
            loadFriendProfile(friendId);
            checkFriendStatus();
        }

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        btnKetban.setOnClickListener(v -> toggleFriendAction());

        btnNhanTin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPrivateChat();
            }
        });
    }

    private void loadFriendProfile(int id) {

        String url = Constants.BASE_URL + "user/by-id?maTaiKhoan=" + id;
        Log.d("API_REQ", "URL gọi API: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    try {
                        Log.d("API_RESP", resp.toString());

                        if (!resp.has("status") || !"success".equals(resp.getString("status"))) {
                            Log.e("API_ERROR", "API trả về lỗi");
                            return;
                        }

                        JSONObject obj = resp.getJSONObject("data");

                        tvName.setText(obj.optString("tenNguoiDung", "Không có tên"));

                        String avatar = obj.optString("anhDaiDien_URL", "");

                        if (!avatar.isEmpty()) {
                            // ⚠️ avatar đang là path tương đối: /uploads/avatars/xxx.png
                            Glide.with(this)
                                    .load(Constants.IMAGE_BASE_URL + avatar)
                                    .error(R.drawable.avatar_default)
                                    .into(imgAvatar);
                        } else {
                            imgAvatar.setImageResource(R.drawable.avatar_default);
                        }

                    } catch (Exception e) {
                        Log.e("API_EXCEPTION", e.getMessage(), e);
                    }
                },
                error -> {
                    Log.e("API_ERROR", error.toString());
                    if (error.networkResponse != null) {
                        Log.e("API_ERROR", "Status: " + error.networkResponse.statusCode);
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }




//    private void checkFriendStatus() {
//
//        if (myId == friendId) {
//            btnKetban.setVisibility(View.GONE);
//            btnNhanTin.setVisibility(View.GONE);
//            return;
//        }
//
//        String url = Constants.BASE_URL +
//                "check_friend_status.php?myId=" + myId + "&friendId=" + friendId;
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                response -> {
//                    try {
//                        String status = response.getString("status");
//
//                        switch (status) {
//                            case "not_friend":
//                                btnKetban.setText("Kết bạn");
//                                btnNhanTin.setVisibility(View.GONE);
//                                break;
//
//                            case "pending":
//                                btnKetban.setText("Hủy yêu cầu");
//                                btnNhanTin.setVisibility(View.GONE);
//                                break;
//
//                            case "friend":
//                                btnKetban.setText("Hủy kết bạn");
//                                btnNhanTin.setVisibility(View.VISIBLE);
//                                break;
//                        }
//
//                    } catch (Exception ignored) {}
//                }, error -> {});
//        Volley.newRequestQueue(this).add(request);
//    }

    private void checkFriendStatus() {

        if (myId == friendId) {
            btnKetban.setVisibility(View.GONE);
            btnNhanTin.setVisibility(View.GONE);
            return;
        }

        String url = Constants.BASE_URL +
                "friends/status?myId=" + myId + "&friendId=" + friendId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String status = response.getString("status");

                        if ("not_friend".equals(status)) {
                            btnKetban.setText("Kết bạn");
                            btnNhanTin.setVisibility(View.GONE);
                        } else if ("pending".equals(status)) {
                            btnKetban.setText("Hủy yêu cầu");
                            btnNhanTin.setVisibility(View.GONE);
                        } else if ("friend".equals(status)) {
                            btnKetban.setText("Hủy kết bạn");
                            btnNhanTin.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("FRIEND_STATUS", error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }



//    private void toggleFriendAction() {
//        String uiText = btnKetban.getText().toString();
//        String action = "";
//
//        switch (uiText) {
//            case "Kết bạn":
//                action = "send_request";
//                break;
//
//            case "Hủy yêu cầu":
//                action = "cancel_request";
//                break;
//
//            case "Hủy kết bạn":
//                action = "unfriend";
//                break;
//        }
//
//        String url = Constants.BASE_URL + "friend_action.php";
//
//        JSONObject body = new JSONObject();
//        try {
//            body.put("myId", myId);
//            body.put("friendId", friendId);
//            body.put("action", action);
//        } catch (Exception ignored) {}
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
//                resp -> checkFriendStatus(),
//                error -> {}
//        );
//
//        Volley.newRequestQueue(this).add(request);
//    }

    private void toggleFriendAction() {
        String uiText = btnKetban.getText().toString();

        switch (uiText) {
            case "Kết bạn":
                action = "send_request";
                sendFriendAction();   // gửi ngay
                break;

            case "Hủy yêu cầu":
                action = "cancel_request";
                sendFriendAction();   // gửi ngay
                break;

            case "Hủy kết bạn":
                showUnfriendStep1();  // hiển thị xác nhận 2 bước
                break;
        }
    }

    private void showUnfriendStep1() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn hủy kết bạn với người này không?")
                .setPositiveButton("Có", (dialog, which) -> showUnfriendStep2())
                .setNegativeButton("Không", null)
                .show();
    }

    private void showUnfriendStep2() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage("Hủy kết bạn sẽ xóa toàn bộ tin nhắn giữa hai bạn. Bạn chắc chắn muốn tiếp tục?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    action = "unfriend";
                    sendFriendAction();   // thực hiện unfriend thật sự
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

//    private void sendFriendAction() {
//
//        Log.d("FRIEND_ACTION", "➡ Sending action: " + action +
//                " | myId=" + myId + " | friendId=" + friendId);
//
//        String url = Constants.BASE_URL + "friend_action.php";
//        Log.d("FRIEND_ACTION", "➡ API URL: " + url);
//
//        // Sử dụng StringRequest để gửi dữ liệu dạng x-www-form-urlencoded
//        StringRequest request = new StringRequest(Request.Method.POST, url,
//                response -> {
//                    Log.d("FRIEND_ACTION", "✅ Server response: " + response);
//                    try {
//                        JSONObject resp = new JSONObject(response);
//                        String status = resp.getString("status");
//                        Log.d("FRIEND_ACTION", "➡ Status response: " + status);
//
//                        if (status.equals("success") && action.equals("send_request")) {
//                            Log.d("FRIEND_ACTION", "📡 Sending WebSocket friend_request...");
//                            sendFriendRequestRealtime(myId, friendId);
//                        } else if (action.equals("cancel_request") || action.equals("unfriend")) {
//                            sendFriendCancelRealtime(myId, friendId);
//                        }
//
//                        // CẬP NHẬT SQLITE KHI UNFRIEND
//                        if (action.equals("unfriend")) {
//                            ChatDatabaseHelper db = new ChatDatabaseHelper(this);
//                            db.deleteFriend(friendId);
//                        }
//
//                        Log.d("FRIEND_ACTION", "🔄 Updating UI after action...");
//                        checkFriendStatus();
//
//                    } catch (Exception e) {
//                        Log.e("FRIEND_ACTION", "❌ Error parsing response: " + e.getMessage());
//                    }
//                },
//                error -> {
//                    if (error.networkResponse != null) {
//                        Log.e("FRIEND_ACTION", "❌ Volley error: " + error.toString() +
//                                " | Status code: " + error.networkResponse.statusCode);
//                    } else {
//                        Log.e("FRIEND_ACTION", "❌ Volley error: " + error.toString());
//                    }
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("myId", String.valueOf(myId));
//                params.put("friendId", String.valueOf(friendId));
//                params.put("action", action);
//                return params;
//            }
//        };
//
//        Log.d("FRIEND_ACTION", "📤 Sending request to server...");
//        Volley.newRequestQueue(this).add(request);
//    }

    private void sendFriendAction() {

        String endpoint = "";

        switch (action) {
            case "send_request":
                endpoint = "friends/send-request";
                break;
            case "cancel_request":
                endpoint = "friends/cancel-request";
                break;
            case "unfriend":
                endpoint = "friends/unfriend";
                break;
        }

        String url = Constants.BASE_URL + endpoint;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject resp = new JSONObject(response);

                        if ("success".equals(resp.getString("status"))) {

                            if ("send_request".equals(action)) {
                                sendFriendRequestRealtime(myId, friendId);
                            } else {
                                sendFriendCancelRealtime(myId, friendId);
                            }

                            if ("unfriend".equals(action)) {
                                new ChatDatabaseHelper(this).deleteFriend(friendId);
                            }

                            checkFriendStatus();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("FRIEND_ACTION", error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("myId", String.valueOf(myId));
                params.put("friendId", String.valueOf(friendId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    private void openPrivateChat() {
        String url = Constants.BASE_URL + "create_room.php";

        SessionManager sm = new SessionManager(this);
        String token = sm.getToken();

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("OPEN_CHAT", "Server response: " + response);

                    try {
                        JSONObject obj = new JSONObject(response);

                        // Nếu backend trả trực tiếp maPhongChat
                        if (obj.has("maPhongChat")) {
                            int roomId = obj.getInt("maPhongChat");
                            openChatRoom(roomId);
                            return;
                        }

                        // Nếu backend trả rooms[]
                        if (obj.has("rooms")) {
                            JSONArray arr = obj.getJSONArray("rooms");
                            if (arr.length() > 0) {
                                int roomId = arr.getJSONObject(0).getInt("maPhongChat");
                                openChatRoom(roomId);
                            } else {
                                createNewPrivateRoom(); // không có phòng → tạo mới
                            }
                        } else {
                            createNewPrivateRoom();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                },
                error -> Log.e("OPEN_CHAT", "Volley error: " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("members", "[" + friendId + "]"); // format backend yêu cầu
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void createNewPrivateRoom() {
        String url = Constants.BASE_URL + "create_room.php";
        SessionManager sm = new SessionManager(this);
        String token = sm.getToken();

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("maPhongChat")) {
                            int roomId = obj.getInt("maPhongChat");
                            openChatRoom(roomId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {}
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("members", "[" + friendId + "]");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void openChatRoom(int roomId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("maPhong", roomId);
        intent.putExtra("friendId", friendId);
        intent.putExtra("friendName", tvName.getText().toString());
        startActivity(intent);
    }

    private void sendFriendRequestRealtime(int fromUserId, int toUserId) {
        try {

            // 🔥 Nếu WebSocket chưa kết nối thì kết nối trước
            if (!WebSocketService.getInstance().isConnected()) {
                Log.d("AddFriend", "⚠️ WebSocket chưa kết nối → đang kết nối lại...");
                WebSocketService.getInstance().connect(Constants.WEBSOCKET_URL, fromUserId);

                // ⏳ Delay nhẹ 200ms để server nhận kết nối rồi mới gửi
                new android.os.Handler().postDelayed(() -> {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("type", "friend_request");
                        json.put("fromUser", fromUserId);
                        json.put("toUser", toUserId);
                        json.put("message", "Bạn có lời mời kết bạn mới!");

                        WebSocketService.getInstance().sendJson(json);

                        Log.d("AddFriend", "📡 Gửi WebSocket friend_request (DELAY): " + json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 200);

                return;
            }

            // 🔥 Nếu đã kết nối thì gửi ngay
            JSONObject json = new JSONObject();
            json.put("type", "friend_request");
            json.put("fromUser", fromUserId);
            json.put("toUser", toUserId);
            json.put("message", "Bạn có lời mời kết bạn mới!");

            WebSocketService.getInstance().sendJson(json);

            Log.d("AddFriend", "📡 Gửi WebSocket friend_request (OK): " + json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFriendCancelRealtime(int fromUserId, int toUserId) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "friend_cancel"); // type khác với friend_request
            json.put("fromUser", fromUserId);
            json.put("toUser", toUserId);
            json.put("message", "Người này đã hủy lời mời kết bạn!");

            WebSocketService.getInstance().sendJson(json);
            Log.d("CancelFriend", "📡 Gửi WebSocket friend_cancel: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
