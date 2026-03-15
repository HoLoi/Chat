package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;

import org.json.JSONObject;

public class AddFriendActivity extends AppCompatActivity {

    private Button btnSendRequestAddFriend;
    private EditText edtSAddFriend;
    private SessionManager sessionManager;
    private ImageView ivBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_friend);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSendRequestAddFriend = findViewById(R.id.btnGuiketban);
        edtSAddFriend = findViewById(R.id.edtThemban);
        ivBack = findViewById(R.id.iv_back_add_friend);
        sessionManager = new SessionManager(this);

        int currentUserId = sessionManager.getMaTaiKhoan();
        WebSocketService socketService = WebSocketService.getInstance();
        //  Kết nối WebSocket nếu chưa kết nối
        if (!socketService.isConnected()) {
            socketService.connect(Constants.WEBSOCKET_URL, currentUserId);
            Log.d("AddFriend", "Đang kết nối WebSocket cho user: " + currentUserId);
        }

        btnSendRequestAddFriend.setOnClickListener(view -> sendFriendRequest());

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

//    private void sendFriendRequest() {
//        String email = edtSAddFriend.getText().toString().trim();
//        if (email.isEmpty()) {
//            Toast.makeText(this, "Nhập email người nhận", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        int maNguoiGui = sessionManager.getMaTaiKhoan();
//
//        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST,
//                Constants.BASE_URL + "send_friend_request.php",
//                response -> {
//                    try {
//                        JSONObject json = new JSONObject(response);
//                        String status = json.optString("status");
//                        String message = json.optString("message");
//
//                        if (status.equals("success")) {
//                            edtSAddFriend.getText().clear();
//                            Toast.makeText(this, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
//
//                            //  Lấy userId người nhận từ API (chính xác)
//                            int toUserId = json.optInt("maTaiKhoan2", 0);
//                            if (toUserId > 0) {
//                                sendFriendRequestRealtime(maNguoiGui, toUserId);
//                            }
//                        } else {
//                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> {
//                    Toast.makeText(this, "Lỗi gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
//                }) {
//            @Override
//            protected java.util.Map<String, String> getParams() {
//                java.util.Map<String, String> params = new java.util.HashMap<>();
//                params.put("maTaiKhoan1", String.valueOf(maNguoiGui));
//                params.put("emailNguoiNhan", email);
//                return params;
//            }
//        };
//
//        Volley.newRequestQueue(this).add(request);
//    }

    private void sendFriendRequest() {
        String email = edtSAddFriend.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Nhập email người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "user/get-user-by-email?email=" + email;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    try {
                        if (!resp.getString("status").equals("success")) {
                            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int friendId = resp.getInt("maTaiKhoan");

                        Intent intent = new Intent(AddFriendActivity.this, PersionalPageActivity.class);
                        intent.putExtra("friendId", friendId);
                        startActivity(intent);

                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }



//    // Gửi realtime đến người nhận
//    private void sendFriendRequestRealtime(int fromUserId, int toUserId) {
//        try {
//            JSONObject json = new JSONObject();
//            json.put("type", "friend_request");
//            json.put("fromUser", fromUserId);
//            json.put("toUser", toUserId);
//            json.put("message", "Bạn có lời mời kết bạn mới!");
//
//            WebSocketService.getInstance().sendJson(json);
//            Log.d("AddFriend", " Gửi WebSocket friend_request: " + json);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
