package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.TrangChuActivity;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.network.VolleyMultipartRequest;
import com.example.chatrealtime.network.VolleyMultipartRequest.DataPart;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MenuChatNhomActivity extends AppCompatActivity {

    private static final String TAG = "MenuChatNhomActivity";
    private static final int REQ_PICK_GROUP_AVATAR = 2021;

    private TextView tvTenPhong;
    private TextView tvDeleteLeave;
    private ImageView imgBack;
    private ShapeableImageView imgAvatarRoom;

    private LinearLayout layoutGroupActions;
    private Button btnRenameGroup;
    private Button btnTogglePrivacy;
    private Button btnPendingRequests;
    private Button btnChangeAvatar;
    private Button btnViewMembers;
    private Button btnAddMembers;
    //private TextView tvGroupNote;

    private int maPhong;
    private int myId;
    private boolean isLeader = false;
    private int kieuNhom = 0;

    private final List<Member> cachedMembers = new ArrayList<>();
    private MemberDialogAdapter memberDialogAdapter;
    private ChatDatabaseHelper dbHelper;
    private boolean isFetchingMembers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_chat_nhom);
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
        tvTenPhong.setText(roomName == null ? "Nhóm chat" : roomName);

        imgBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnRenameGroup.setOnClickListener(v -> showRenameDialog());
        btnChangeAvatar.setOnClickListener(v -> chooseGroupAvatar());
        btnViewMembers.setOnClickListener(v -> onViewMembersClicked());
        btnAddMembers.setOnClickListener(v -> showAddMemberDialog());
        btnTogglePrivacy.setOnClickListener(v -> togglePrivacy());
        btnPendingRequests.setOnClickListener(v -> openPendingDialog());
        tvDeleteLeave.setOnClickListener(v -> confirmLeaveOrDissolve());

        loadRoomInfo();
    }

    private void initViews() {
        tvTenPhong = findViewById(R.id.tv_tenphong);
        tvDeleteLeave = findViewById(R.id.tv_delete_leave);
        imgBack = findViewById(R.id.iv_back_menu_chat);
        imgAvatarRoom = findViewById(R.id.iv_menu_avatar);

        layoutGroupActions = findViewById(R.id.layout_group_actions);
        btnRenameGroup = findViewById(R.id.btn_rename_group);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        btnViewMembers = findViewById(R.id.btn_view_members);
        btnAddMembers = findViewById(R.id.btn_add_members);
        btnTogglePrivacy = findViewById(R.id.btn_toggle_privacy);
        btnPendingRequests = findViewById(R.id.btn_pending_requests);
        //tvGroupNote = findViewById(R.id.tv_group_note);
    }

    private void loadRoomInfo() {
        String url = Constants.BASE_URL + "chat/room-info?maPhongChat=" + maPhong + "&maTaiKhoan=" + myId;
        SessionManager session = new SessionManager(this);
        Log.d(TAG, "loadRoomInfo start. roomId=" + maPhong + ", myId=" + myId + ", url=" + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.optString("status", "");
                        Log.d(TAG, "loadRoomInfo response status=" + status + ", body=" + response);
                        if (!"success".equals(status)) {
                            return;
                        }

                        cachedMembers.clear();

                        JSONObject room = response.getJSONObject("room");
                        JSONArray arr = response.optJSONArray("members");
                        if (arr == null) {
                            Log.w(TAG, "loadRoomInfo: members=null trong room-info, thử gọi room-members để fallback");
                            fetchMembersFromServer(false, false);
                            arr = new JSONArray();
                        }

                        int loaiPhong = room.optInt("loaiPhong", 0);
                        boolean isGroup = loaiPhong == 1 || arr.length() > 2;
                        if (!isGroup) {
                            Log.w(TAG, "Menu nhóm nhận phòng 1-1, bỏ qua redirect để tránh nhảy màn hình");
                            return;
                        }

                        Log.d(TAG, "loadRoomInfo parsed. loaiPhong=" + loaiPhong + ", members=" + arr.length());

                        tvTenPhong.setText(room.optString("tenPhongChat", tvTenPhong.getText().toString()));
                        kieuNhom = room.optInt("kieuNhom", 0);
                        int leaderId = room.has("maTruongNhom")
                                ? room.optInt("maTruongNhom")
                                : room.optInt("maTaiKhoanTao", -1);
                        isLeader = (leaderId == myId);

                        String avatarGroup = normalizeAvatarUrl(room.optString("anhDaiDienUrl", room.optString("anhDaiDien_URL", "")));
                        loadAvatarInto(imgAvatarRoom, avatarGroup);

                        setMembersFromResponse(arr);
                        applyMode();
                    } catch (Exception e) {
                        Log.e(TAG, "loadRoomInfo parse error", e);
                    }
                },
                error -> Log.e(TAG, "loadRoomInfo error", error)
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

    private void applyMode() {
        layoutGroupActions.setVisibility(View.VISIBLE);
        btnViewMembers.setVisibility(View.VISIBLE);
        btnAddMembers.setVisibility(View.VISIBLE);

        int leaderVis = isLeader ? View.VISIBLE : View.GONE;
        btnRenameGroup.setVisibility(leaderVis);
        btnChangeAvatar.setVisibility(leaderVis);
        btnTogglePrivacy.setVisibility(leaderVis);
        btnPendingRequests.setVisibility(isLeader && kieuNhom == 1 ? View.VISIBLE : View.GONE);
        //tvGroupNote.setVisibility(leaderVis);

        btnTogglePrivacy.setText(kieuNhom == 1 ? "Chuyển sang công khai" : "Chuyển sang riêng tư");
        tvDeleteLeave.setText(isLeader ? "Giải tán nhóm" : "Rời nhóm");
    }

    private void confirmLeaveOrDissolve() {
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
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                p.put("memberId", String.valueOf(myId));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void dissolveGroup() {
        String url = Constants.BASE_URL + "chat/dissolve-group";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    Toast.makeText(this, "Đã giải tán nhóm", Toast.LENGTH_SHORT).show();
                    navigateToMessageScreen();
                },
                err -> Toast.makeText(this, "Lỗi giải tán nhóm", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
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

    private void setMembersFromResponse(JSONArray arr) throws Exception {
        if (arr == null) {
            Log.w(TAG, "setMembersFromResponse: arr=null");
            return;
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) {
                Log.w(TAG, "setMembersFromResponse: member tại index=" + i + " không hợp lệ");
                continue;
            }

            int id = obj.optInt("maTaiKhoan", obj.optInt("id", -1));
            if (id == -1) {
                Log.w(TAG, "setMembersFromResponse: thiếu maTaiKhoan/id ở index=" + i + ", obj=" + obj);
                continue;
            }

            String name = obj.optString("tenNguoiDung", "");
            String role = obj.optString("vaiTro", "member");
            String avatar = obj.optString("anhDaiDien", obj.optString("anhDaiDien_URL", obj.optString("anhDaiDienUrl", "")));
            cachedMembers.add(new Member(id, name, role, avatar));
        }

        Log.d(TAG, "setMembersFromResponse done. totalCached=" + cachedMembers.size());
    }

    private void onViewMembersClicked() {
        Log.d(TAG, "onViewMembersClicked. cachedMembers=" + cachedMembers.size());
        if (cachedMembers.isEmpty()) {
            fetchMembersFromServer(true, true);
            return;
        }
        showMemberDialog();
    }

    private void fetchMembersFromServer(boolean showAfterLoad, boolean forceReload) {
        if (isFetchingMembers) {
            Log.d(TAG, "fetchMembersFromServer bị bỏ qua vì đang tải");
            if (showAfterLoad) {
                Toast.makeText(this, "Đang tải danh sách thành viên...", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!forceReload && !cachedMembers.isEmpty()) {
            Log.d(TAG, "fetchMembersFromServer dùng cache sẵn có, size=" + cachedMembers.size());
            if (showAfterLoad) {
                showMemberDialog();
            }
            return;
        }

        String url = Constants.BASE_URL + "chat/room-members?maPhongChat=" + maPhong;
        Log.d(TAG, "fetchMembersFromServer start. url=" + url + ", showAfterLoad=" + showAfterLoad + ", forceReload=" + forceReload);
        isFetchingMembers = true;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        String status = res.optString("status", "");
                        Log.d(TAG, "fetchMembersFromServer response status=" + status + ", body=" + res);
                        if (!"success".equals(status)) {
                            if (showAfterLoad) {
                                Toast.makeText(this, res.optString("message", "Không tải được danh sách thành viên"), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        JSONArray members = res.optJSONArray("members");
                        cachedMembers.clear();
                        setMembersFromResponse(members);

                        if (showAfterLoad) {
                            if (cachedMembers.isEmpty()) {
                                Toast.makeText(this, "Không có thành viên để hiển thị", Toast.LENGTH_SHORT).show();
                            } else {
                                showMemberDialog();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "fetchMembersFromServer parse error", e);
                        if (showAfterLoad) {
                            Toast.makeText(this, "Lỗi đọc dữ liệu thành viên", Toast.LENGTH_SHORT).show();
                        }
                    } finally {
                        isFetchingMembers = false;
                    }
                },
                err -> {
                    Log.e(TAG, "fetchMembersFromServer network error", err);
                    if (showAfterLoad) {
                        Toast.makeText(this, "Lỗi mạng khi tải thành viên", Toast.LENGTH_SHORT).show();
                    }
                    isFetchingMembers = false;
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void showRenameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Tên nhóm mới");
        new AlertDialog.Builder(this)
                .setTitle("Đổi tên nhóm")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) updateRoom(name, null, null);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void togglePrivacy() {
        int newKieu = kieuNhom == 1 ? 0 : 1;
        updateRoom(null, newKieu, null);
    }

    private void updateRoom(String newName, Integer newKieu, String avatarUrl) {
        String url = Constants.BASE_URL + "chat/update-room";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    Toast.makeText(this, "Đã cập nhật phòng", Toast.LENGTH_SHORT).show();
                    loadRoomInfo();
                },
                err -> Toast.makeText(this, "Lỗi cập nhật phòng", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                if (newName != null) p.put("tenPhong", newName);
                if (newKieu != null) p.put("kieuNhom", String.valueOf(newKieu));
                if (avatarUrl != null) p.put("anhDaiDien", avatarUrl);
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

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
        String url = Constants.BASE_URL + "chat/upload-file";
        VolleyMultipartRequest req = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    try {
                        String resStr = new String(response.data);
                        JSONObject obj = new JSONObject(resStr);
                        if ("success".equals(obj.optString("status"))) {
                            String fileUrl = obj.optString("duongDanFile", "");
                            if (!fileUrl.isEmpty()) {
                                updateRoom(null, null, fileUrl);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                },
                error -> Toast.makeText(this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoanGui", String.valueOf(myId));
                p.put("loaiTinNhan", "image");
                p.put("skipMessage", "true");
                return p;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    byte[] bytes = readBytes(uri);
                    params.put("file", new DataPart("avatar.jpg", bytes, "image/jpeg"));
                } catch (Exception ignored) {
                }
                return params;
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
            Log.w(TAG, "showMemberDialog: cache rỗng, tự tải lại danh sách");
            fetchMembersFromServer(true, true);
            return;
        }

        Log.d(TAG, "showMemberDialog: hiển thị dialog với " + cachedMembers.size() + " thành viên");

        memberDialogAdapter = new MemberDialogAdapter(this, cachedMembers);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thành viên")
                .setAdapter(memberDialogAdapter, (d, which) -> {
                    Member m = cachedMembers.get(which);
                    Intent intent = new Intent(this, PersionalPageActivity.class);
                    intent.putExtra("friendId", m.id);
                    startActivity(intent);
                })
                .create();

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
        ArrayList<JSONObject> friends = dbHelper.getFriends();
        Set<Integer> currentMemberIds = new HashSet<>();
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

        List<JSONObject> available = new ArrayList<>();
        for (JSONObject f : friends) {
            int id = f.optInt("maTaiKhoan", f.optInt("maNguoiDung"));
            if (!currentMemberIds.contains(id)) {
                available.add(f);
            }
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "Bạn bè đã vào nhóm hết rồi", Toast.LENGTH_SHORT).show();
            return;
        }

        android.widget.ArrayAdapter<JSONObject> adapter = new android.widget.ArrayAdapter<>(this, R.layout.item_member_dialog, available) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.item_member_dialog, parent, false);
                }
                JSONObject f = getItem(position);
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
                    Glide.with(MenuChatNhomActivity.this)
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
                    JSONObject chosen = available.get(idx);
                    int chosenId = chosen.optInt("maTaiKhoan", chosen.optInt("maNguoiDung"));
                    addMember(chosenId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addMember(int memberId) {
        String url = Constants.BASE_URL + "chat/add-member";
        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
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
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
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
                        List<JSONObject> list = new ArrayList<>();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) list.add(arr.getJSONObject(i));
                        }
                        showPendingList(list);
                    } catch (Exception ignored) {
                    }
                },
                err -> Toast.makeText(this, "Không tải được yêu cầu", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    private void showPendingList(List<JSONObject> list) {
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
                loadAvatarInto(iv, avatarFull);
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
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
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
                    for (int i = 0; i < cachedMembers.size(); i++) {
                        if (cachedMembers.get(i).id == memberId) {
                            cachedMembers.remove(i);
                            break;
                        }
                    }
                    if (memberDialogAdapter != null) memberDialogAdapter.notifyDataSetChanged();
                    loadRoomInfo();
                },
                err -> Toast.makeText(this, "Lỗi xóa thành viên", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("maPhongChat", String.valueOf(maPhong));
                p.put("maTaiKhoan", String.valueOf(myId));
                p.put("memberId", String.valueOf(memberId));
                return p;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

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
    }

    private class MemberDialogAdapter extends android.widget.ArrayAdapter<Member> {
        private final List<Member> data;

        MemberDialogAdapter(android.content.Context ctx, List<Member> items) {
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
            loadAvatarInto(iv, full);

            return v;
        }
    }

    private void loadAvatarInto(ImageView imageView, String normalizedUrl) {
        if (normalizedUrl == null || normalizedUrl.isEmpty()) {
            Glide.with(this).clear(imageView);
            imageView.setImageResource(R.drawable.avatar_default);
            return;
        }

        Glide.with(this)
                .load(normalizedUrl)
                .placeholder(R.drawable.avatar_default)
                .error(R.drawable.avatar_default)
                .into(imageView);
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
