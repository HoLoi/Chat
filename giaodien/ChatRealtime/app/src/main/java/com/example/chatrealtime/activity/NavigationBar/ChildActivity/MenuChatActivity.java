package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.example.chatrealtime.activity.TrangChuActivity;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MenuChatActivity extends AppCompatActivity {

    private static final String TAG = "MenuChatActivity";

    private LinearLayout layoutTrangCaNhan;
    private TextView tvTenPhong;
    private TextView tvDelete;
    private ImageView imgBack;
    private ShapeableImageView imgAvatarRoom;

    private int maPhong;
    private int myId;
    private int friendId = -1;
    private ChatDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        maPhong = getIntent().getIntExtra("maPhong", -1);
        if (maPhong == -1) {
            Toast.makeText(this, "Không tìm thấy phòng chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        SessionManager sessionManager = new SessionManager(this);
        myId = sessionManager.getMaTaiKhoan();
        dbHelper = new ChatDatabaseHelper(this);

        String roomName = getIntent().getStringExtra("roomName");
        tvTenPhong.setText(roomName == null ? "Đoạn chat" : roomName);

        friendId = getIntent().getIntExtra("friendId", -1);

        imgBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        layoutTrangCaNhan.setOnClickListener(v -> openPersonalPage());
        tvDelete.setOnClickListener(v -> confirmDeleteRoom());

        loadRoomInfo();
    }

    private void initViews() {
        layoutTrangCaNhan = findViewById(R.id.lnlayout_trangcanhan);
        tvTenPhong = findViewById(R.id.tv_tenphong);
        tvDelete = findViewById(R.id.tv_delete_leave);
        imgBack = findViewById(R.id.iv_back_menu_chat);
        imgAvatarRoom = findViewById(R.id.iv_menu_avatar);
    }

    private void loadRoomInfo() {
        String url = Constants.BASE_URL + "chat/room-info?maPhongChat=" + maPhong + "&maTaiKhoan=" + myId;
        SessionManager session = new SessionManager(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!"success".equals(response.optString("status"))) return;

                        JSONObject room = response.getJSONObject("room");
                        JSONArray members = response.optJSONArray("members");

                        int loaiPhong = room.optInt("loaiPhong", 0);
                        boolean isGroup = loaiPhong == 1 || (members != null && members.length() > 2);
                        if (isGroup) {
                            android.util.Log.w(TAG, "Menu 1-1 nhận phòng nhóm, bỏ qua redirect để tránh nhảy màn hình");
                            return;
                        }

                        updateOneToOneHeader(members);
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "loadRoomInfo parse error", e);
                    }
                },
                error -> android.util.Log.e(TAG, "loadRoomInfo error", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + session.getToken());
                return headers;
            }

            @Override
            protected com.android.volley.Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String json = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(new JSONObject(json), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updateOneToOneHeader(JSONArray members) {
        if (members == null) return;

        for (int i = 0; i < members.length(); i++) {
            JSONObject obj = members.optJSONObject(i);
            if (obj == null) continue;

            int id = obj.optInt("maTaiKhoan", -1);
            if (id == -1 || id == myId) continue;

            friendId = id;
            String friendName = obj.optString("tenNguoiDung", "").trim();
            if (!friendName.isEmpty()) {
                tvTenPhong.setText(friendName);
            }

            String rawAvatar = obj.optString("anhDaiDien", obj.optString("anhDaiDien_URL", ""));
            String avatar = normalizeAvatarUrl(rawAvatar);
            Glide.with(this)
                    .load(avatar.isEmpty() ? null : avatar)
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(imgAvatarRoom);
            return;
        }
    }

    private void openPersonalPage() {
        if (friendId != -1) {
            Intent intent = new Intent(this, PersionalPageActivity.class);
            intent.putExtra("friendId", friendId);
            startActivity(intent);
            return;
        }

        String url = Constants.BASE_URL + "chat/room-members?maPhongChat=" + maPhong;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        if (!"success".equals(res.optString("status"))) {
                            Toast.makeText(this, "Không lấy được danh sách thành viên", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = res.optJSONArray("members");
                        if (arr == null) return;

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            int id = o.optInt("maTaiKhoan", -1);
                            if (id != -1 && id != myId) {
                                friendId = id;
                                Intent intent = new Intent(this, PersionalPageActivity.class);
                                intent.putExtra("friendId", friendId);
                                startActivity(intent);
                                return;
                            }
                        }

                        Toast.makeText(this, "Không tìm thấy người chat đối diện", Toast.LENGTH_SHORT).show();
                    } catch (Exception ignored) {
                        Toast.makeText(this, "Lỗi lấy thông tin thành viên", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Lỗi mạng khi lấy thành viên", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(req);
    }

    private void confirmDeleteRoom() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc muốn xóa cuộc trò chuyện này?")
                .setPositiveButton("Xóa", (d, w) -> executeDeleteRoom())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeDeleteRoom() {
        String url = Constants.BASE_URL + "chat/delete-room";
        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if ("success".equals(jsonObject.optString("status", "error"))) {
                            if (dbHelper != null) {
                                dbHelper.deleteRoom(maPhong);
                            }
                            Toast.makeText(this, "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                            navigateToMessageScreen();
                        } else {
                            Toast.makeText(this,
                                    "Lỗi: " + com.example.chatrealtime.network.ServerMessageDecoder.normalize(jsonObject.optString("message", "Không thể xóa")),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xử lý dữ liệu xóa phòng", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("maPhongChat", String.valueOf(maPhong));
                params.put("maTaiKhoan", String.valueOf(myId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void navigateToMessageScreen() {
        Intent intent = new Intent(this, TrangChuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String normalizeAvatarUrl(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "/null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("http")) return trimmed;
        if (trimmed.startsWith("/")) return Constants.IMAGE_BASE_URL + trimmed;
        return Constants.IMAGE_BASE_URL + "/" + trimmed;
    }
}
