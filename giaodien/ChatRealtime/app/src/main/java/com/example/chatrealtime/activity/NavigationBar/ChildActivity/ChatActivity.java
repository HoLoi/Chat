package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.adapter.MessageAdapter;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.Message;
import com.example.chatrealtime.model.MessageModerationStatus;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;
import com.example.chatrealtime.network.VolleyMultipartRequest;
import com.example.chatrealtime.network.VolleyMultipartRequest.DataPart;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    public static int CURRENT_OPEN_ROOM = -1;

    private RecyclerView recyclerMessages;
    private TextView txtNameChat;
    private EditText edtMessage;
    private ImageView btnSend;
    private ImageView btnBack, btnMenu;
    private ImageButton btnAttach;

    private MessageAdapter adapter;
    private final List<Message> messageList = new ArrayList<>();

    private SessionManager session;
    private int roomId;
    private int maTaiKhoan;
    private String roomName;
    private boolean isGroupChat = false;
    private boolean hasResolvedRoomType = false;
    private ChatDatabaseHelper dbHelper;

    private static final String BASE_URL = Constants.BASE_URL;
    private static final int REQ_PICK_MEDIA = 1010;
    private static final int MAX_UPLOAD_BYTES = 10 * 1024 * 1024; // 10MB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d("CHAT_OPEN", "Extras chatId=" + getIntent().getStringExtra("chatId") + " maPhong=" + getIntent().getIntExtra("maPhong", -1) + " maPhongChat=" + getIntent().getIntExtra("maPhongChat", -1) + " roomName=" + getIntent().getStringExtra("roomName"));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initSession();
        setupRecyclerView();

        dbHelper = new ChatDatabaseHelper(this);

        // Nhận từ notification/bubble: ưu tiên "maPhong", fallback "chatId"
        roomId = getIntent().getIntExtra("maPhong", -1);
        if (roomId == -1) {
            roomId = getIntent().getIntExtra("chatId", -1);
        }
        if (roomId == -1) {
            roomId = getIntent().getIntExtra("maPhongChat", -1);
        }
        if (roomId == -1) {
            Toast.makeText(this, "Không tìm thấy phòng chat", Toast.LENGTH_SHORT).show();
            // Không finish để tránh thoát app đột ngột; chỉ dừng init WebSocket/loader
            return;
        }

        // Load tin nhắn OFFLINE theo đúng room hiện tại
        List<Message> offlineMessages = dbHelper.getMessages(roomId, maTaiKhoan);
        if (!offlineMessages.isEmpty()) {
            adapter.setMessages(offlineMessages);
            recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
        }

        roomName = getIntent().getStringExtra("roomName");
        String friendName = getIntent().getStringExtra("friendName");
        boolean isGroupFromIntent = getIntent().getBooleanExtra("isGroup", false);
        isGroupChat = isGroupFromIntent;
        if (!isGroupFromIntent && friendName != null && !friendName.isEmpty()) {
            roomName = friendName;
        } else if (roomName == null || roomName.isEmpty()) {
            roomName = "Đoạn chat";
        }
        txtNameChat.setText(roomName);
        resolveRoomDisplayNameFromServer();

        CURRENT_OPEN_ROOM = roomId;

        loadMessages();
        setupWebSocket();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (roomId != -1) {
            markAsReadImmediately(roomId);
        }
    }

    private void initViews() {
        txtNameChat = findViewById(R.id.tv_NameChat);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnback);
        btnMenu = findViewById(R.id.iv_menu);
        btnAttach = findViewById(R.id.btnAttach);
    }

    private void initSession() {
        session = new SessionManager(this);
        maTaiKhoan = session.getMaTaiKhoan();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(this, messageList, maTaiKhoan);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);
    }

    /**
     *  Kết nối WebSocket và join phòng
     */
    private void setupWebSocket() {
        WebSocketService ws = WebSocketService.getInstance();

        if (!ws.isConnected()) {
            ws.connect(Constants.WEBSOCKET_URL, maTaiKhoan);
        }

        // Join room sau khi kết nối
        try {
            JSONObject join = new JSONObject();
            join.put("type", "join_room");
            join.put("userId", maTaiKhoan);
            join.put("roomId", roomId);
            ws.sendJson(join);
            Log.d("WS_JOIN", "Joined room " + roomId);
        } catch (Exception e) {
            Log.e("WS_JOIN_ERR", "Lỗi join phòng: " + e.getMessage());
        }

        //  Lắng nghe tin nhắn realtime
        ws.getMessageLiveData().observe(this, newMessageObserver());
    }

    /**
     *  Gắn sự kiện click cho các nút
     */
    private void setupActions() {
        btnSend.setOnClickListener(v -> {
            String msg = edtMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
            }
        });

        btnAttach.setOnClickListener(v -> openMediaPicker());

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("OPEN_MENU", " Mở menu chat cho phòng " + roomId);
                openProperMenu();
            }
        });
    }

    private void openProperMenu() {
        if (hasResolvedRoomType) {
            startMenuByRoomType(isGroupChat);
            return;
        }

        String url = BASE_URL + "chat/room-info"
                + "?maPhongChat=" + roomId
                + "&maTaiKhoan=" + maTaiKhoan;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    boolean fallbackGroup = isGroupChat;
                    try {
                        JSONObject obj = new JSONObject(response.trim());
                        if ("success".equalsIgnoreCase(obj.optString("status"))) {
                            JSONObject room = obj.optJSONObject("room");
                            JSONArray members = obj.optJSONArray("members");
                            fallbackGroup = isGroupRoom(room, members);
                        }
                    } catch (Exception e) {
                        Log.e("OPEN_MENU", "Không parse được room-info để mở menu", e);
                    }

                    isGroupChat = fallbackGroup;
                    hasResolvedRoomType = true;
                    startMenuByRoomType(isGroupChat);
                },
                error -> {
                    Log.e("OPEN_MENU", "Không tải được room-info để mở menu: " + error);
                    resolveRoomTypeByMembersAndOpenMenu();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + session.getToken());
                return headers;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String parsed = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(parsed, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void resolveRoomTypeByMembersAndOpenMenu() {
        String url = BASE_URL + "chat/room-members?maPhongChat=" + roomId;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    boolean fallbackGroup = isGroupChat;
                    try {
                        if ("success".equalsIgnoreCase(response.optString("status"))) {
                            JSONArray members = response.optJSONArray("members");
                            fallbackGroup = members != null && members.length() > 2;
                        }
                    } catch (Exception e) {
                        Log.e("OPEN_MENU", "Không parse được room-members để mở menu", e);
                    }

                    isGroupChat = fallbackGroup;
                    hasResolvedRoomType = true;
                    startMenuByRoomType(isGroupChat);
                },
                error -> {
                    Log.e("OPEN_MENU", "Không tải được room-members để mở menu: " + error);
                    Toast.makeText(this, "Không xác định được loại phòng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void startMenuByRoomType(boolean isGroup) {
        Intent intent = new Intent(
                ChatActivity.this,
                isGroup ? MenuChatNhomActivity.class : MenuChatActivity.class
        );
        intent.putExtra("maPhong", roomId);
        intent.putExtra("roomName", roomName);
        intent.putExtra("isGroup", isGroup);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     *  Gửi tin nhắn qua WebSocket và lưu DB
     */
    private void sendMessage(String msg) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "chat_message");
            json.put("maTaiKhoanGui", maTaiKhoan);
            json.put("maPhongChat", roomId);
            json.put("noiDung", msg);
            json.put("loaiTinNhan", "text");

            btnSend.setEnabled(false);
            sendMessageApi(json, null, "text", msg);
        } catch (Exception e) {
            Log.e("SEND_MSG_ERR", " Lỗi gửi tin nhắn: " + e.getMessage(), e);
            btnSend.setEnabled(true);
        }
    }

    /**
     *  Gửi request POST để lưu tin nhắn vào MySQL
     */
    private void sendMessageApi(JSONObject messageJson, String duongDanFile, String loaiTinNhan, String noiDungFallback) {
        String url = BASE_URL + "chat/send-message";
        Log.d("SEND_API", " Gửi request lưu DB tới: " + url);

        String noiDungLocal = noiDungFallback != null ? noiDungFallback : messageJson.optString("noiDung", "");
        String loaiTinNhanLocal = loaiTinNhan != null ? loaiTinNhan : messageJson.optString("loaiTinNhan", "text");

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("SEND_API", " Phản hồi từ server: " + response);
                    try {
                        JSONObject obj = new JSONObject(response.trim());
                        MessageModerationStatus status = MessageModerationStatus.from(obj.optString("status"));
                        double score = obj.optDouble("score", 0.0);
                        Log.d("SEND_API_STATUS", "Status=" + status + " score=" + score);
                        handleModerationOutcome(status, noiDungLocal, loaiTinNhanLocal, duongDanFile);
                    } catch (Exception e) {
                        Log.e("SEND_API_PARSE", " Lỗi phân tích phản hồi: " + e.getMessage());
                        Toast.makeText(this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                        btnSend.setEnabled(true);
                    }
                },
                error -> {
                    Log.e("SEND_API_ERR", " Lỗi khi lưu DB: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        showBannedDialog();
                    } else if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                            JSONObject obj = new JSONObject(body.trim());
                            MessageModerationStatus status = MessageModerationStatus.from(obj.optString("status"));
                            if (status == MessageModerationStatus.BLOCK) {
                                showBlockDialog();
                            } else {
                                Toast.makeText(this,
                                        com.example.chatrealtime.network.ServerMessageDecoder.normalize(obj.optString("message", "Gửi tin nhắn thất bại")),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                    }
                    btnSend.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("maPhongChat", String.valueOf(messageJson.getInt("maPhongChat")));
                    params.put("maTaiKhoanGui", String.valueOf(messageJson.getInt("maTaiKhoanGui")));
                    params.put("noiDung", noiDungFallback != null ? noiDungFallback : messageJson.optString("noiDung", ""));
                    params.put("loaiTinNhan", loaiTinNhan != null ? loaiTinNhan : messageJson.optString("loaiTinNhan", "text"));
                    if (duongDanFile != null) {
                        params.put("duongDanFile", duongDanFile);
                    }
                    Log.d("SEND_API_PARAMS", "Params gửi đi: " + params);
                } catch (Exception e) {
                    Log.e("PARAM_ERR", "Lỗi tạo params: " + e.getMessage());
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + session.getToken());
                return headers;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String parsed = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(parsed, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void handleModerationOutcome(MessageModerationStatus status, String noiDung, String loaiTinNhan, String duongDanFile) {
        switch (status) {
            case BANNED:
                showBannedDialog();
                btnSend.setEnabled(true);
                return;
            case BLOCK:
                showBlockDialog();
                btnSend.setEnabled(true);
                return;
            case WARNING:
                appendMessageToUi(noiDung, loaiTinNhan, duongDanFile, status);
                Toast.makeText(this, "Tin nhắn có nội dung nhạy cảm", Toast.LENGTH_SHORT).show();
                break;
            case CLEAN:
            default:
                appendMessageToUi(noiDung, loaiTinNhan, duongDanFile, MessageModerationStatus.CLEAN);
                sendRealtime(noiDung, loaiTinNhan, duongDanFile);
                break;
        }
        edtMessage.setText("");
        btnSend.setEnabled(true);
    }

    private void appendMessageToUi(String noiDung, String loaiTinNhan, String duongDanFile, MessageModerationStatus status) {
        String nowTime = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
        Message msg = new Message(null, noiDung, true, maTaiKhoan, null, loaiTinNhan, duongDanFile, nowTime, "sent", null, session.getEmail(), status);
        adapter.addMessage(msg);
        recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
        dbHelper.addMessage(roomId, null, maTaiKhoan, null, noiDung, loaiTinNhan, duongDanFile, nowTime, "sent", status.name());
    }

    private void sendRealtime(String noiDung, String loaiTinNhan, String duongDanFile) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "chat_message");
            json.put("maTaiKhoanGui", maTaiKhoan);
            json.put("maPhongChat", roomId);
            json.put("noiDung", noiDung != null ? noiDung : "");
            json.put("loaiTinNhan", loaiTinNhan != null ? loaiTinNhan : "text");
            if (duongDanFile != null) json.put("duongDanFile", duongDanFile);
            WebSocketService.getInstance().sendJson(json);
        } catch (Exception e) {
            Log.e("WS_SEND", " Lỗi gửi realtime: " + e.getMessage());
        }
    }

    private void showBlockDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tin nhắn bị chặn")
                .setMessage("Tin nhắn vi phạm chính sách và đã bị chặn")
                .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                .show();
    }

    private void showBannedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tài khoản bị khóa")
                .setMessage("Tài khoản đã bị khóa do vi phạm nhiều lần")
                .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                .show();
    }

    private void resolveRoomDisplayNameFromServer() {
        String url = BASE_URL + "chat/room-info"
                + "?maPhongChat=" + roomId
                + "&maTaiKhoan=" + maTaiKhoan;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response.trim());
                        if (!"success".equalsIgnoreCase(obj.optString("status"))) {
                            return;
                        }

                        JSONObject room = obj.optJSONObject("room");
                        JSONArray members = obj.optJSONArray("members");

                        boolean isGroup = isGroupRoom(room, members);
                        isGroupChat = isGroup;
                        hasResolvedRoomType = true;
                        String resolvedName;
                        if (isGroup) {
                            resolvedName = firstNonEmpty(
                                    room != null ? room.optString("tenPhongChat", "") : "",
                                    roomName,
                                    "Nhóm chat"
                            );
                        } else {
                            resolvedName = resolveOneToOneName(members);
                            if (resolvedName == null || resolvedName.trim().isEmpty()) {
                                resolvedName = firstNonEmpty(
                                        getIntent().getStringExtra("friendName"),
                                        roomName,
                                        "Đoạn chat"
                                );
                            }
                        }

                        roomName = resolvedName;
                        txtNameChat.setText(resolvedName);
                    } catch (Exception e) {
                        Log.e("ROOM_NAME", "Không parse được room-info", e);
                    }
                },
                error -> Log.e("ROOM_NAME", "Không tải được room-info: " + error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + session.getToken());
                return headers;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String parsed = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(parsed, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private boolean isGroupRoom(JSONObject room, JSONArray members) {
        if (room != null) {
            Object loaiPhongObj = room.opt("loaiPhong");
            if (loaiPhongObj instanceof Boolean) {
                return (Boolean) loaiPhongObj;
            }
            if (loaiPhongObj instanceof Number) {
                return ((Number) loaiPhongObj).intValue() != 0;
            }
            if (loaiPhongObj instanceof String) {
                String raw = ((String) loaiPhongObj).trim().toLowerCase(Locale.ROOT);
                if ("1".equals(raw) || "true".equals(raw)) {
                    return true;
                }
            }
        }

        return members != null && members.length() > 2;
    }

    private String resolveOneToOneName(JSONArray members) {
        if (members == null) return "";

        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.optJSONObject(i);
            if (member == null) continue;

            int memberId = member.optInt("maTaiKhoan", member.optInt("id", -1));
            if (memberId != -1 && memberId != maTaiKhoan) {
                return member.optString("tenNguoiDung", "").trim();
            }
        }

        return "";
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return "";
    }

    /**
     *  Tải lịch sử tin nhắn của phòng
     */
    private void loadMessages() {
        //String url = BASE_URL + "get_messages.php?maPhongChat=" + roomId + "&maTaiKhoan=" + maTaiKhoan;
        String url = BASE_URL + "chat/messages"
                + "?maPhongChat=" + roomId
                + "&maTaiKhoan=" + maTaiKhoan;

        Log.d("LOAD_MSG", " Tải tin nhắn từ: " + url);

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response.trim());
                        Log.d("LOAD_MSG_RES", "Phản hồi tin nhắn: " + obj);

                        if ("success".equals(obj.optString("status"))) {
                            JSONArray arr = obj.optJSONArray("messages");
                            List<Message> tmp = new ArrayList<>();
                            boolean hasUnreadIncoming = false;
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject item = arr.getJSONObject(i);
                                String noiDung = item.optString("noiDung", "");
                                int maTinNhan = item.optInt("maTinNhan", -1);
                                int maGui = item.optInt("maTaiKhoanGui", -1);
                                boolean isMine = (maGui == maTaiKhoan);
                                String loaiTinNhan = item.optString("loaiTinNhan", "text");
                                String fileUrl = item.optString("duongDanFile", null);
                                if (fileUrl != null && (fileUrl.isEmpty() || "null".equals(fileUrl))) {
                                    fileUrl = null;
                                }
                                String avatar = item.optString("anhDaiDienNguoiGui", null);
                                if (avatar != null && (avatar.isEmpty() || "null".equalsIgnoreCase(avatar))) {
                                    avatar = null;
                                }
                                String tenNguoiGui = item.optString("tenNguoiGui", "");
                                String thoiGianGui = item.optString("thoiGianGui", "");
                                String trangThaiTinNhan = item.optString("messageStatus", "sent");

                                if (!isMine && !"read".equalsIgnoreCase(trangThaiTinNhan)) {
                                    hasUnreadIncoming = true;
                                }

                                String moderationRaw = item.optString("trangThaiKiemDuyet", "");
                                MessageModerationStatus moderation = MessageModerationStatus.from(moderationRaw);

                                Integer msgId = maTinNhan > 0 ? maTinNhan : null;
                                tmp.add(new Message(msgId, noiDung, isMine, maGui, null, loaiTinNhan, fileUrl, thoiGianGui, trangThaiTinNhan, avatar, tenNguoiGui, moderation));
                            }
                            adapter.setMessages(tmp);
                            recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);

                            // Chỉ mark-read khi thật sự còn tin đến chưa đọc để tránh vòng lặp websocket.
                            if (hasUnreadIncoming) {
                                markAsReadImmediately(roomId);
                            }

                            // QUAN TRỌNG: Lưu danh sách chuẩn từ Server vào SQLite
                            dbHelper.saveMessages(roomId, tmp);
                        } else {
                            //Toast.makeText(this, obj.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("LOAD_MSG_ERR", "Lỗi đọc phản hồi: " + e.getMessage());
                    }
                },
                error -> Log.e("LOAD_MSG_ERR", "Lỗi tải tin nhắn: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + session.getToken());
                return h;
            }

            @Override
            protected com.android.volley.Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String parsed = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(parsed, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.ParseError(e));
                }
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    /**
     *  Nhận tin nhắn realtime qua WebSocket
     */
    private Observer<String> newMessageObserver() {
        return text -> {
            try {
                JSONObject obj = new JSONObject(text.trim());
                String type = obj.optString("type");

                if ("message_status_update".equals(type)) {
                    int updatedRoom = obj.optInt("maPhongChat", -1);
                    if (updatedRoom == roomId) {
                        loadMessages();
                    }
                    return;
                }

                if (!"chat_message".equals(type)) return;

                int maPhongChat = obj.optInt("maPhongChat", -1);
                if (maPhongChat != roomId) return;

                int maGui = obj.optInt("maTaiKhoanGui", -1);

                if (maGui == maTaiKhoan) {
                    Log.d("WS_SKIP", "⏭ Bỏ qua tin nhắn của chính mình.");
                    return;
                }

                String noiDung = obj.optString("noiDung", "");
                boolean isMine = (maGui == maTaiKhoan);
                String loaiTinNhan = obj.optString("loaiTinNhan", "text");
                String fileUrl = obj.optString("duongDanFile", null);
                if (fileUrl != null && (fileUrl.isEmpty() || "null".equals(fileUrl))) {
                    fileUrl = null;
                }
                String avatar = obj.optString("anhDaiDienNguoiGui", null);
                if (avatar != null && (avatar.isEmpty() || "null".equalsIgnoreCase(avatar))) {
                    avatar = null;
                }
                String tenNguoiGui = obj.optString("tenNguoiGui", "");
                String thoiGianGui = obj.optString("thoiGianGui", "");

                Log.d("WS_NEW_MSG", "Tin nhắn realtime: " + noiDung + " | Từ: " + maGui);

                Message last = adapter.getLastMessage();
                if (isSameMessage(last, maGui, noiDung, loaiTinNhan, fileUrl)) {
                    Log.d("WS_SKIP", "⏭ Bỏ qua tin nhắn realtime trùng lặp.");
                    return;
                }

                adapter.addMessage(new Message(null, noiDung, isMine, maGui, maTaiKhoan, loaiTinNhan, fileUrl, thoiGianGui, "sent", avatar, tenNguoiGui, MessageModerationStatus.CLEAN));
                recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);

                // LƯU TIN NHẮN ĐẾN VÀO SQLITE
                dbHelper.addMessage(roomId, null, maGui, maTaiKhoan, noiDung, loaiTinNhan, fileUrl, thoiGianGui, "sent", null);

                // Đang mở phòng này → đánh dấu đã đọc ngay để reset unread
                markAsReadImmediately(roomId);

            } catch (Exception e) {
                Log.e("WS_MSG_ERR", "Lỗi nhận tin realtime: " + e.getMessage());
            }
        };
    }

    private boolean isSameMessage(Message last, int maGui, String noiDung, String loaiTinNhan, String fileUrl) {
        if (last == null) {
            return false;
        }
        if (last.getMaNguoiGui() != maGui) {
            return false;
        }
        String lastNoiDung = last.getNoiDung() != null ? last.getNoiDung() : "";
        String lastLoai = last.getLoaiTinNhan() != null ? last.getLoaiTinNhan() : "text";
        String lastFile = last.getDuongDanFile() != null ? last.getDuongDanFile() : "";
        String curNoiDung = noiDung != null ? noiDung : "";
        String curLoai = loaiTinNhan != null ? loaiTinNhan : "text";
        String curFile = fileUrl != null ? fileUrl : "";
        return lastNoiDung.equals(curNoiDung)
                && lastLoai.equalsIgnoreCase(curLoai)
                && lastFile.equals(curFile);
    }

    /**
     * Gọi API đánh dấu đã đọc ngay lập tức cho 1 tin nhắn cụ thể hoặc cả phòng
     */
    private void markAsReadImmediately(int maPhong) {
        String url = Constants.BASE_URL + "chat/mark-read";

        Log.d("MARK_READ", "POST " + url + " maPhongChat=" + maPhong + " maTaiKhoan=" + session.getMaTaiKhoan());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("MARK_READ", "Đã đánh dấu đọc tin mới"),
                error -> Log.e("MARK_READ", "Lỗi đánh dấu đọc: " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("maPhongChat", String.valueOf(maPhong));
                params.put("maTaiKhoan", String.valueOf(session.getMaTaiKhoan()));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + session.getToken());
                return headers;
            }
        };

        // Thêm vào hàng đợi gửi đi ngay
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (roomId != -1) {
            markAsReadImmediately(roomId);
        }
        CURRENT_OPEN_ROOM = -1;
    }

    private void openMediaPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQ_PICK_MEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_MEDIA && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    uploadSelectedMedia(uri);
                }
            } else {
                Uri uri = data.getData();
                if (uri != null) {
                    uploadSelectedMedia(uri);
                }
            }
        }
    }

    private void uploadSelectedMedia(Uri uri) {
        try {
            String mime = getContentResolver().getType(uri);
            String loaiTinNhan = (mime != null && mime.startsWith("video")) ? "video" : "image";
            String fileName = getFileNameFromUri(uri);
            byte[] fileData = readBytes(uri);

            if (fileData == null || fileData.length == 0) {
                Toast.makeText(this, "Không đọc được file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fileData.length > MAX_UPLOAD_BYTES) {
                double mb = fileData.length / 1024.0 / 1024.0;
                Toast.makeText(this, String.format("File %.1fMB vượt giới hạn 10MB", mb), Toast.LENGTH_SHORT).show();
                return;
            }

            String url = BASE_URL + "chat/upload-file";
            Log.d("UPLOAD_MEDIA", "Upload " + fileName + " size=" + fileData.length);

            VolleyMultipartRequest req = new VolleyMultipartRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            String resStr = new String(response.data);
                            Log.d("UPLOAD_MEDIA", "Phản hồi: " + resStr);
                            JSONObject obj = new JSONObject(resStr);
                            MessageModerationStatus status = MessageModerationStatus.from(obj.optString("status"));
                            String fileUrl = obj.optString("duongDanFile", null);
                            String type = obj.optString("loaiTinNhan", loaiTinNhan);
                            if (fileUrl == null || fileUrl.isEmpty()) {
                                Toast.makeText(this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            handleModerationOutcome(status, "", type, fileUrl);
                        } catch (Exception e) {
                            Log.e("UPLOAD_MEDIA", "Lỗi parse: " + e.getMessage());
                            Toast.makeText(this, "Upload lỗi", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("UPLOAD_MEDIA", "Lỗi: " + error.toString());
                        if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                            showBannedDialog();
                        } else if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                                JSONObject obj = new JSONObject(body.trim());
                                MessageModerationStatus status = MessageModerationStatus.from(obj.optString("status"));
                                if (status == MessageModerationStatus.BLOCK) {
                                    showBlockDialog();
                                } else {
                                    Toast.makeText(this,
                                            com.example.chatrealtime.network.ServerMessageDecoder.normalize(obj.optString("message", "Upload thất bại")),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {
                                Toast.makeText(this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("maPhongChat", String.valueOf(roomId));
                    params.put("maTaiKhoanGui", String.valueOf(maTaiKhoan));
                    params.put("loaiTinNhan", loaiTinNhan);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + session.getToken());
                    return headers;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("file", new DataPart(fileName, fileData, mime != null ? mime : "application/octet-stream"));
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            Log.e("UPLOAD_MEDIA", "lỗi " + e.getMessage(), e);
            Toast.makeText(this, "Upload lỗi", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "file";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx != -1) {
                    result = cursor.getString(idx);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    private byte[] readBytes(Uri uri) {
        try {
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                 java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
                byte[] data = new byte[8192];
                int nRead;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                return buffer.toByteArray();
            }
        } catch (Exception e) {
            Log.e("READ_URI", "lỗi " + e.getMessage());
            return null;
        }
    }
}
