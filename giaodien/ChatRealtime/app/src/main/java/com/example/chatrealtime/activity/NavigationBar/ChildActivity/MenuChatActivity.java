package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.net.Uri;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.TrangChuActivity;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.network.VolleyMultipartRequest;
import com.example.chatrealtime.network.VolleyMultipartRequest.DataPart;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class MenuChatActivity extends AppCompatActivity {

    private static final String TAG = "MenuChatActivity";

    LinearLayout layoutTrangCaNhan;
    TextView tvTenPhong;
    ImageView imgBack;
    ShapeableImageView imgAvatarRoom;
    ListView lvMembers;
    TextView tvMemberHeader;
    TextView tvDeleteLeave;
    LinearLayout layoutGroupActions;
    Button btnRenameGroup, btnTogglePrivacy, btnPendingRequests, btnChangeAvatar, btnViewMembers, btnAddMembers;
    TextView tvGroupNote;

    private int friendId = -1;
    private boolean isGroup = false;
    private boolean isLeader = false;
    private int kieuNhom = 0; // 0 public, 1 private
    private int maPhong;
    private int myId;
    private java.util.List<Member> cachedMembers = new java.util.ArrayList<>();
    private AlertDialog memberDialogRef;
    private MemberDialogAdapter memberDialogAdapter;
    private MemberListAdapter memberListAdapter;
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
        String tenPhong = getIntent().getStringExtra("roomName");
        friendId = getIntent().getIntExtra("friendId", -1);

        layoutTrangCaNhan = findViewById(R.id.lnlayout_trangcanhan);
        tvTenPhong = findViewById(R.id.tv_tenphong);
        imgBack = findViewById(R.id.iv_back_menu_chat);
        imgAvatarRoom = findViewById(R.id.iv_menu_avatar);
        lvMembers = findViewById(R.id.lv_members);
        tvMemberHeader = findViewById(R.id.tv_member_header);
        tvDeleteLeave = findViewById(R.id.tv_delete_leave);
        layoutGroupActions = findViewById(R.id.layout_group_actions);
        btnRenameGroup = findViewById(R.id.btn_rename_group);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        btnViewMembers = findViewById(R.id.btn_view_members);
        btnAddMembers = findViewById(R.id.btn_add_members);
        btnTogglePrivacy = findViewById(R.id.btn_toggle_privacy);
        btnPendingRequests = findViewById(R.id.btn_pending_requests);
        tvGroupNote = findViewById(R.id.tv_group_note);

        tvTenPhong.setText(tenPhong);

        SessionManager sessionManager = new SessionManager(this);
        myId = sessionManager.getMaTaiKhoan();
        dbHelper = new ChatDatabaseHelper(this);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        loadRoomInfo();

        layoutTrangCaNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPersonalPage();
            }
        });

        btnRenameGroup.setOnClickListener(v -> showRenameDialog());
        btnChangeAvatar.setOnClickListener(v -> chooseGroupAvatar());
        btnViewMembers.setOnClickListener(v -> showMemberDialog());
        btnAddMembers.setOnClickListener(v -> showAddMemberDialog());
        btnTogglePrivacy.setOnClickListener(v -> togglePrivacy());
        btnPendingRequests.setOnClickListener(v -> openPendingDialog());
        tvDeleteLeave.setOnClickListener(v -> confirmLeaveOrDissolve());

        lvMembers.setVisibility(View.GONE); // không hiển thị list trong menu chính

    }

    private void loadRoomInfo() {
        String url = Constants.BASE_URL + "chat/room-info?maPhongChat=" + maPhong + "&maTaiKhoan=" + myId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        android.util.Log.d(TAG, "loadRoomInfo status=" + response.optString("status") + " room=" + maPhong);
                        if (!response.getString("status").equals("success")) return;

                        cachedMembers.clear();
                        friendId = -1;

                        JSONObject room = response.getJSONObject("room");
                        JSONArray arr = response.getJSONArray("members");

                        tvTenPhong.setText(room.optString("tenPhongChat", tvTenPhong.getText().toString()));

                        int loaiPhong = room.optInt("loaiPhong", 0);
                        isGroup = (loaiPhong == 1 || arr.length() > 2);
                        kieuNhom = room.optInt("kieuNhom", 0);
                        int leaderId = room.has("maTruongNhom") ? room.optInt("maTruongNhom") : room.optInt("maTaiKhoanTao", -1);
                        isLeader = (leaderId == myId);
                        applyMode();

                        // Ưu tiên camelCase trả từ API, fallback snake_case để tránh miss-key
                        String avatarGroup = normalizeAvatarUrl(room.optString("anhDaiDienUrl", room.optString("anhDaiDien_URL", "")));
                        if (isGroup) {
                            Glide.with(this)
                                    .load(avatarGroup.isEmpty() ? null : avatarGroup)
                                    .placeholder(R.drawable.avatar_default)
                                    .error(R.drawable.avatar_default)
                                    .into(imgAvatarRoom);
                        }

                        setMembersFromResponse(arr, avatarGroup);

                        updateOneToOneHeader();

                        bindMemberList();
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "loadRoomInfo parse error", e);
                    }
                },
                error -> android.util.Log.e(TAG, "loadRoomInfo error", error)
        );

        queue.add(request);
    }

    private void applyMode() {
        if (isGroup) {
            layoutTrangCaNhan.setVisibility(View.GONE);
            tvMemberHeader.setVisibility(View.VISIBLE);
            lvMembers.setVisibility(View.GONE);
            tvDeleteLeave.setText(isLeader ? "Giải tán nhóm" : "Rời nhóm");
            layoutGroupActions.setVisibility(View.VISIBLE);
            // Hiển thị chung: xem thành viên, thêm thành viên
            btnViewMembers.setVisibility(View.VISIBLE);
            btnAddMembers.setVisibility(View.VISIBLE);
            // Chỉ trưởng nhóm mới thấy các thao tác quản trị
            int leaderVis = isLeader ? View.VISIBLE : View.GONE;
            btnRenameGroup.setVisibility(leaderVis);
            btnChangeAvatar.setVisibility(leaderVis);
            btnTogglePrivacy.setVisibility(leaderVis);
            btnPendingRequests.setVisibility(isLeader && kieuNhom == 1 ? View.VISIBLE : View.GONE);
            tvGroupNote.setVisibility(leaderVis);
            btnTogglePrivacy.setText(kieuNhom == 1 ? "Chuyển sang Public" : "Chuyển sang Private");
        } else {
            layoutTrangCaNhan.setVisibility(View.VISIBLE);
            tvMemberHeader.setVisibility(View.GONE);
            lvMembers.setVisibility(View.GONE);
            tvDeleteLeave.setText("Xóa cuộc trò chuyện");
            layoutGroupActions.setVisibility(View.GONE);
        }
    }

    private void confirmLeaveOrDissolve() {
        if (!isGroup) {
            confirmDeleteRoom();
            return;
        }
        if (isLeader) {
            new AlertDialog.Builder(this)
                    .setTitle("Giải tán nhóm")
                    .setMessage("Giải tán nhóm sẽ xóa toàn bộ phòng chat. Tiếp tục?")
                    .setPositiveButton("Giải tán", (d, w) -> dissolveGroup())
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Rời nhóm")
                    .setMessage("Bạn chắc chắn muốn rời nhóm?")
                    .setPositiveButton("Rời nhóm", (d, w) -> leaveGroup())
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    private void leaveGroup() {
        String url = Constants.BASE_URL + "chat/remove-member";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    Toast.makeText(this, "Đã rời nhóm", Toast.LENGTH_SHORT).show();
                    navigateToMessageScreen();
                },
                err -> Toast.makeText(this, "Lỗi rời nhóm", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                p.put("memberId", String.valueOf(myId));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void dissolveGroup() {
        String url = Constants.BASE_URL + "chat/delete-room";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    String msg = isGroup ? "Đã giải tán nhóm" : "Đã xóa cuộc trò chuyện";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    navigateToMessageScreen();
                },
                err -> Toast.makeText(this, "Lỗi giải tán nhóm", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void navigateToMessageScreen() {
        Intent intent = new Intent(this, TrangChuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmDeleteRoom() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc muốn xóa cuộc trò chuyện này?")
                .setPositiveButton("Xóa", (d, w) -> dissolveGroup())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setMembersFromResponse(JSONArray arr, String avatarGroup) throws Exception {
        boolean friendAvatarSet = false;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            int id = obj.getInt("maTaiKhoan");
            String name = obj.optString("tenNguoiDung", "");
            String role = obj.optString("vaiTro", "member");
            // server có thể trả anhDaiDien hoặc anhDaiDien_URL
            String avatar = obj.optString("anhDaiDien", obj.optString("anhDaiDien_URL", ""));
            cachedMembers.add(new Member(id, name, role, avatar));

            // Phòng 1-1: luôn lấy avatar của người đối diện ngay khi load danh sách
            if (!isGroup && id != myId && !friendAvatarSet) {
                String friendAvatar = normalizeAvatarUrl(avatar);
                Glide.with(this)
                        .load(friendAvatar.isEmpty() ? null : friendAvatar)
                        .placeholder(R.drawable.avatar_default)
                        .error(R.drawable.avatar_default)
                        .into(imgAvatarRoom);
                friendAvatarSet = true;
            }
        }

        if (isGroup) {
            String groupAvatar = normalizeAvatarUrl(avatarGroup);
            Glide.with(this)
                    .load(groupAvatar.isEmpty() ? null : groupAvatar)
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(imgAvatarRoom);
        }

        // Lấy friendId cho chat 1-1
        if (!isGroup) {
            for (Member m : cachedMembers) {
                if (m.id != myId) {
                    friendId = m.id;
                    break;
                }
            }
        }
    }

    private void showRenameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Tên nhóm mới");
        new AlertDialog.Builder(this)
                .setTitle("Đổi tên nhóm")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        updateRoom(name, null);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void togglePrivacy() {
        int newKieu = kieuNhom == 1 ? 0 : 1;
        updateRoom(null, newKieu);
    }

    private void updateRoom(String newName, Integer newKieu) {
        String url = Constants.BASE_URL + "chat/update-room";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    loadRoomInfo();
                    Toast.makeText(this, "Đã cập nhật phòng", Toast.LENGTH_SHORT).show();
                },
                err -> Toast.makeText(this, "Lỗi cập nhật phòng", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                if (newName != null) p.put("tenPhong", newName);
                if (newKieu != null) p.put("kieuNhom", String.valueOf(newKieu));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private static final int REQ_PICK_GROUP_AVATAR = 2021;

    private void chooseGroupAvatar() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_GROUP_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_GROUP_AVATAR && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) uploadGroupAvatar(uri);
        }
    }

    private void uploadGroupAvatar(Uri uri) {
        try {
            String url = Constants.BASE_URL + "chat/upload-file";
            VolleyMultipartRequest req = new VolleyMultipartRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            String resStr = new String(response.data);
                            JSONObject obj = new JSONObject(resStr);
                            if ("success".equals(obj.optString("status"))) {
                                String fileUrl = obj.optString("duongDanFile", "");
                                if (!fileUrl.isEmpty()) {
                                    updateRoomAvatar(fileUrl);
                                }
                            }
                        } catch (Exception ignored) {}
                    },
                    error -> Toast.makeText(this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> p = new java.util.HashMap<>();
                    p.put("maPhongChat", String.valueOf(maPhong));
                    p.put("maTaiKhoanGui", String.valueOf(myId));
                    p.put("loaiTinNhan", "image");
                    // Chỉ upload file, không tạo tin nhắn trong phòng
                    p.put("skipMessage", "true");
                    return p;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new java.util.HashMap<>();
                    try {
                        byte[] bytes = readBytes(uri);
                        params.put("file", new DataPart("avatar.jpg", bytes, "image/jpeg"));
                    } catch (Exception ignored) {}
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            Toast.makeText(this, "Không chọn được ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRoomAvatar(String avatarUrl) {
        String url = Constants.BASE_URL + "chat/update-room";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> loadRoomInfo(),
                err -> Toast.makeText(this, "Lỗi đổi ảnh nhóm", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                p.put("anhDaiDien", avatarUrl);
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private byte[] readBytes(Uri uri) throws Exception {
        try (java.io.InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return new byte[0];
            return is.readAllBytes();
        }
    }

    private void showMemberDialog() {
        if (cachedMembers.isEmpty()) {
            Toast.makeText(this, "Chưa tải danh sách thành viên", Toast.LENGTH_SHORT).show();
            return;
        }
        memberDialogAdapter = new MemberDialogAdapter(this, cachedMembers);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thành viên")
            .setAdapter(memberDialogAdapter, (d, which) -> {
                    Member m = cachedMembers.get(which);
                    Intent intent = new Intent(MenuChatActivity.this, PersionalPageActivity.class);
                    intent.putExtra("friendId", m.id);
                    startActivity(intent);
                })
                .create();
        memberDialogRef = dialog;
        dialog.show();

        if (isLeader) {
            dialog.getListView().setOnItemLongClickListener((parent, view, position, id) -> {
                Member m = cachedMembers.get(position);
                if (m.id == myId) return true;
                confirmRemoveMember(m.id, m.name);
                return true;
            });
        }
    }

    private void showAddMemberDialog() {
        java.util.ArrayList<org.json.JSONObject> friends = dbHelper.getFriends();
        java.util.Set<Integer> currentMemberIds = new java.util.HashSet<>();
        for (Member m : cachedMembers) {
            currentMemberIds.add(m.id);
        }

        if (friends.isEmpty()) {
            final EditText input = new EditText(this);
            input.setHint("Nhập mã tài khoản cần thêm");
            new AlertDialog.Builder(this)
                    .setTitle("Thêm thành viên")
                    .setView(input)
                    .setPositiveButton("Thêm", (d, w) -> {
                        String raw = input.getText().toString().trim();
                        if (raw.isEmpty()) return;
                        int candidate = Integer.parseInt(raw);
                        if (currentMemberIds.contains(candidate)) {
                            Toast.makeText(this, "Thành viên đã có trong nhóm", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addMember(candidate);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return;
        }

        java.util.List<org.json.JSONObject> available = new java.util.ArrayList<>();
        for (org.json.JSONObject f : friends) {
            int id = f.optInt("maTaiKhoan", f.optInt("maNguoiDung"));
            if (!currentMemberIds.contains(id)) {
                available.add(f);
            }
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "Bạn bè đã vào nhóm hết rồi", Toast.LENGTH_SHORT).show();
            return;
        }

        android.widget.ArrayAdapter<org.json.JSONObject> adapter = new android.widget.ArrayAdapter<>(this, R.layout.item_member_dialog, available) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.item_member_dialog, parent, false);
                }
                org.json.JSONObject f = getItem(position);
                ImageView iv = v.findViewById(R.id.ivAvatar);
                TextView tv = v.findViewById(R.id.tvName);
                int id = f.optInt("maTaiKhoan", f.optInt("maNguoiDung"));
                String name = f.optString("hoTen", "Bạn bè") + " (" + id + ")";
                tv.setText(name);

                String rawAvatar = f.optString("anhDaiDien", f.optString("anhDaiDien_URL", ""));
                String full = normalizeAvatarUrl(rawAvatar);
                if (full.isEmpty()) {
                    iv.setImageResource(R.drawable.avatar_default);
                } else {
                    Glide.with(MenuChatActivity.this)
                            .load(full)
                            .placeholder(R.drawable.avatar_default)
                            .error(R.drawable.avatar_default)
                            .into(iv);
                }
                return v;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn bạn để thêm")
                .setAdapter(adapter, (d, idx) -> {
                    org.json.JSONObject chosen = available.get(idx);
                    int chosenId = chosen.optInt("maTaiKhoan", chosen.optInt("maNguoiDung"));
                    android.util.Log.d("MenuChatActivity", "Add member click id=" + chosenId);
                    addMember(chosenId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addMember(int memberId) {
        android.util.Log.d("MenuChatActivity", "Calling addMember memberId=" + memberId + " room=" + maPhong + " myId=" + myId);
        String url = Constants.BASE_URL + "chat/add-member";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    android.util.Log.d("MenuChatActivity", "addMember resp=" + resp);
                    try {
                        JSONObject obj = new JSONObject(resp);
                        String status = obj.optString("status", "success");
                        String message = obj.optString("message", "Đã xử lý");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (!"pending".equalsIgnoreCase(status)) {
                            loadRoomInfo();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Đã thêm thành viên", Toast.LENGTH_SHORT).show();
                        loadRoomInfo();
                    }
                },
                err -> {
                    android.util.Log.e("MenuChatActivity", "addMember error", err);
                    try {
                        if (err.networkResponse != null && err.networkResponse.data != null) {
                            String body = new String(err.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                            JSONObject obj = new JSONObject(body.trim());
                            String msg = obj.optString("message", "Lỗi thêm thành viên");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi thêm thành viên", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, "Lỗi thêm thành viên", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                    p.put("memberId", String.valueOf(memberId));
                return p;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String utf8 = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(utf8, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void openPersonalPage() {
        android.util.Log.d(TAG, "openPersonalPage friendId=" + friendId + " room=" + maPhong);
        if (friendId != -1) {
            Intent intent = new Intent(MenuChatActivity.this, PersionalPageActivity.class);
            intent.putExtra("friendId", friendId);
            startActivity(intent);
            return;
        }

        // Nếu chưa có friendId (phòng 1-1), gọi API room-members để lấy ra người còn lại
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
                                android.util.Log.d(TAG, "openPersonalPage resolved friendId=" + friendId);
                                Intent intent = new Intent(MenuChatActivity.this, PersionalPageActivity.class);
                                intent.putExtra("friendId", friendId);
                                startActivity(intent);
                                return;
                            }
                        }
                        Toast.makeText(this, "Không tìm thấy người chát đối diện", Toast.LENGTH_SHORT).show();
                    } catch (Exception ignored) {
                        Toast.makeText(this, "Lỗi lấy thông tin thành viên", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Lỗi mạng khi lấy thành viên", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(req);
    }

    private void openPendingDialog() {
        String url = Constants.BASE_URL + "group/pending?maPhongChat=" + maPhong + "&maTaiKhoan=" + myId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        if (!"success".equals(res.optString("status"))) {
                            Toast.makeText(this, res.optString("message", "Không có quyền xem yêu cầu"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray arr = res.optJSONArray("requests");
                        java.util.List<JSONObject> list = new java.util.ArrayList<>();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) list.add(arr.getJSONObject(i));
                        }
                        android.util.Log.d(TAG, "pending size=" + list.size() + " room=" + maPhong);
                        showPendingList(list);
                    } catch (Exception ignored) {}
                },
                err -> Toast.makeText(this, "Không tải được yêu cầu", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void showPendingList(java.util.List<JSONObject> list) {
        if (list.isEmpty()) {
            Toast.makeText(this, "Không có yêu cầu chờ", Toast.LENGTH_SHORT).show();
            return;
        }
        android.widget.ArrayAdapter<JSONObject> adapter = new android.widget.ArrayAdapter<>(this, R.layout.item_member_dialog, list) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.item_member_dialog, parent, false);
                }
                JSONObject o = getItem(position);
                ImageView iv = v.findViewById(R.id.ivAvatar);
                TextView tv = v.findViewById(R.id.tvName);

                String displayName = o.optString("tenNguoiDung", "");
                String name = displayName.isEmpty() ? o.optString("maTaiKhoan", "") : displayName;
                String status = o.optString("trangThai", "pending");
                tv.setText(name + " - " + status);

                String avatarRaw = o.optString("anhDaiDien", "");
                String avatarFull = normalizeAvatarUrl(avatarRaw);
                Glide.with(MenuChatActivity.this)
                        .load(avatarFull.isEmpty() ? null : avatarFull)
                        .placeholder(R.drawable.avatar_default)
                        .error(R.drawable.avatar_default)
                        .into(iv);
                return v;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu tham gia")
                .setAdapter(adapter, (dialog, which) -> {
                    JSONObject chosen = list.get(which);
                    int requestId = chosen.optInt("id", chosen.optInt("maTaiKhoan", -1));
                    if (requestId != -1) showApproveDialog(requestId);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showApproveDialog(int requestId) {
        new AlertDialog.Builder(this)
                .setTitle("Duyệt yêu cầu")
                .setMessage("Chấp nhận thành viên này?")
                .setPositiveButton("Chấp nhận", (d, w) -> approveRequest(requestId, true))
                .setNegativeButton("Từ chối", (d, w) -> approveRequest(requestId, false))
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void approveRequest(int requestId, boolean accept) {
        String url = Constants.BASE_URL + "group/approve";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    Toast.makeText(this, "Đã xử lý", Toast.LENGTH_SHORT).show();
                    loadRoomInfo();
                },
                err -> Toast.makeText(this, "Lỗi xử lý", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("idYeuCau", String.valueOf(requestId));
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("nguoiXuLy", String.valueOf(myId));
                p.put("action", accept ? "approve" : "reject");
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void confirmRemoveMember(int memberId, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thành viên")
                .setMessage("Xóa " + name + " khỏi nhóm?")
                .setPositiveButton("Xóa", (dialog, which) -> removeMember(memberId))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeMember(int memberId) {
        String url = Constants.BASE_URL + "chat/remove-member";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    Toast.makeText(this, "Đã xóa thành viên", Toast.LENGTH_SHORT).show();
                    // Cập nhật ngay danh sách đang hiển thị
                    for (int i = 0; i < cachedMembers.size(); i++) {
                        if (cachedMembers.get(i).id == memberId) {
                            cachedMembers.remove(i);
                            break;
                        }
                    }
                    if (memberDialogAdapter != null) memberDialogAdapter.notifyDataSetChanged();
                    if (memberListAdapter != null) memberListAdapter.notifyDataSetChanged();
                    loadRoomInfo();
                },
                err -> Toast.makeText(this, "Lỗi xóa thành viên", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                p.put("memberId", String.valueOf(memberId));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    // Model và adapter cho dialog thành viên
    private static class Member {
        final int id;
        final String name;
        final String role;
        final String avatar;
        Member(int id, String name, String role, String avatar) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.avatar = avatar;
        }
        @Override public String toString() { return name; }
    }

    private class MemberDialogAdapter extends android.widget.ArrayAdapter<Member> {
        private final java.util.List<Member> data;
        MemberDialogAdapter(android.content.Context ctx, java.util.List<Member> items) {
            super(ctx, 0, items);
            this.data = items;
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.item_member_dialog, parent, false);
            }
            Member m = data.get(position);
            ImageView iv = v.findViewById(R.id.ivAvatar);
            TextView tv = v.findViewById(R.id.tvName);
            tv.setText(m.role.equalsIgnoreCase("admin") ? m.name + " (Trưởng nhóm)" : m.name);
            String full = normalizeAvatarUrl(m.avatar);
            if (full.isEmpty()) {
                full = null;
            }
            Glide.with(getContext())
                    .load(full)
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(iv);
            return v;
        }
    }

    private void bindMemberList() {
        if (!isGroup) {
            lvMembers.setAdapter(null);
            return;
        }
        memberListAdapter = new MemberListAdapter();
        lvMembers.setAdapter(memberListAdapter);
        lvMembers.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!isLeader) return true;
            Member m = cachedMembers.get(position);
            if (m.id == myId) return true;
            confirmRemoveMember(m.id, m.name);
            return true;
        });
    }

    private class MemberListAdapter extends android.widget.BaseAdapter {
        @Override
        public int getCount() { return cachedMembers.size(); }

        @Override
        public Object getItem(int position) { return cachedMembers.get(position); }

        @Override
        public long getItemId(int position) { return cachedMembers.get(position).id; }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.item_member_dialog, parent, false);
            }
            Member m = cachedMembers.get(position);
            ImageView iv = v.findViewById(R.id.ivAvatar);
            TextView tv = v.findViewById(R.id.tvName);
            tv.setText(m.role.equalsIgnoreCase("admin") ? m.name + " (Trưởng nhóm)" : m.name);
            Glide.with(MenuChatActivity.this)
                    .load(normalizeAvatarUrl(m.avatar).isEmpty() ? null : normalizeAvatarUrl(m.avatar))
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(iv);

            // Ẩn nút xóa cho thành viên thường
            v.setOnClickListener(view -> {
                Intent intent = new Intent(MenuChatActivity.this, PersionalPageActivity.class);
                intent.putExtra("friendId", m.id);
                startActivity(intent);
            });
            return v;
        }
    }

    private String normalizeAvatarUrl(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "/null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("http")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return Constants.IMAGE_BASE_URL + trimmed;
        }
        // Trường hợp server trả về đường dẫn tương đối (uploads/...) thì tự thêm base
        return Constants.IMAGE_BASE_URL + "/" + trimmed;
    }

    private void updateOneToOneHeader() {
        if (isGroup) return;
        for (Member m : cachedMembers) {
            if (m.id != myId) {
                friendId = m.id;
                tvTenPhong.setText(m.name);
                String avatar = normalizeAvatarUrl(m.avatar);
                Glide.with(this)
                        .load(avatar.isEmpty() ? null : avatar)
                        .placeholder(R.drawable.avatar_default)
                        .error(R.drawable.avatar_default)
                        .into(imgAvatarRoom);
                return;
            }
        }
    }
}