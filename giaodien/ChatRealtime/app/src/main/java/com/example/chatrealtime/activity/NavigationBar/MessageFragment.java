package com.example.chatrealtime.activity.NavigationBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChatActivity;
import com.example.chatrealtime.adapter.RoomAdapter;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.*;
import org.json.*;

import java.util.*;

public class MessageFragment extends Fragment {

    private ListView listViewPhongchat;
    private RoomAdapter roomAdapter;
    private List<Room> roomList;
    private ChatDatabaseHelper dbHelper;
    private EditText etSearch;
    private ArrayList<JSONObject> searchResults = new ArrayList<>();
    private boolean isVisibleToUser = false; // Chỉ xử lý realtime khi fragment đang hiện

    private static final String TAG = "MessageFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_message, container, false);

        listViewPhongchat = root.findViewById(R.id.lv_phongchat);
        etSearch = root.findViewById(R.id.et_search_message);
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(requireContext(), roomList);
        listViewPhongchat.setAdapter(roomAdapter);

        SessionManager session = new SessionManager(requireContext());
        int maTaiKhoan = session.getMaTaiKhoan();

        // Khởi tạo Database Helper
        dbHelper = new ChatDatabaseHelper(requireContext());

        // BƯỚC 1: Load dữ liệu OFFLINE từ SQLite trước (Hiển thị ngay lập tức)
        List<Room> offlineRooms = dbHelper.getRooms();
        if (!offlineRooms.isEmpty()) {
            roomList.clear();
            roomList.addAll(offlineRooms);
            roomAdapter.notifyDataSetChanged();
        }

        // BƯỚC 2: Gọi API lấy dữ liệu mới nhất
        loadRoomsFromApi();

        // Search realtime
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String kw = s.toString().trim();
                if (kw.isEmpty()) {
                    searchResults.clear();
                    listViewPhongchat.setAdapter(roomAdapter);
                    roomAdapter.notifyDataSetChanged();
                } else {
                    searchRooms(kw, maTaiKhoan);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Kết nối WebSocket
        WebSocketService ws = WebSocketService.getInstance();
        if (!ws.isConnected()) {
            ws.connect(Constants.WEBSOCKET_URL, maTaiKhoan);
        }

        // Nhận realtime
        ws.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            try {
                JSONObject obj = new JSONObject(message);
                if (!"chat_message".equals(obj.optString("type"))) return;

                int maPhong = obj.optInt("maPhongChat", -1);
                String noiDung = obj.optString("noiDung", "");

                // Lấy ID người gửi để biết có nên tăng số tin nhắn chưa đọc hay không
                int senderId = obj.optInt("maTaiKhoanGui", -1);

                if (isVisibleToUser) {
                    updateRoomWithNewMessage(maPhong, noiDung, senderId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Parse realtime message error: " + e.getMessage());
            }
        });

        // Khi click phòng → mở ChatActivity
        listViewPhongchat.setOnItemClickListener((parent, view, position, id) -> {
            if (!searchResults.isEmpty()) {
                JSONObject obj = searchResults.get(position);
                String type = obj.optString("type", "room");
                int targetId = obj.optInt("id", obj.optInt("maTaiKhoan", -1));
                String name = obj.optString("tenPhongChat", obj.optString("tenNguoiDung", ""));
                if (targetId <= 0) return;
                if ("room".equals(type)) {
                    openRoom(targetId, name);
                } else {
                    openPrivateChat(targetId, name);
                }
                return;
            }

            Room room = roomList.get(position);

            room.resetUnread();
            roomAdapter.notifyDataSetChanged();
            markAsReadOnServer(room.getId());
            // Lưu lại trạng thái đã đọc để không bị max() với giá trị cũ
            dbHelper.saveRooms(roomList);

            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("maPhong", room.getId());
            intent.putExtra("roomName", room.getName());
            startActivity(intent);
        });

        listViewPhongchat.setOnItemLongClickListener((parent, view, position, id) -> {
            Room room = roomList.get(position);

            // Tạo PopupMenu
            PopupMenu popup = new PopupMenu(requireContext(), view);
            popup.getMenuInflater().inflate(R.menu.room_options_menu, popup.getMenu());

            // Xử lý click menu
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete_room) {
                    // Xóa phòng chat
                    confirmAndDeleteRoom(room);
                    return true;
                } else if (itemId == R.id.action_unfriend) {
                    // Hủy kết bạn với người trong phòng 1-1
                    //confirmAndUnfriend(room);
                    return true;
                }
                return false;
            });

            popup.show();
            return true; // Tránh sự kiện click bình thường
        });


        return root;
    }

    // Hiển thị hộp thoại xác nhận trước khi xóa
    private void confirmAndDeleteRoom(Room room) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc chắn muốn xóa cuộc trò chuyện với " + room.getName() + " không?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi hàm xóa thật sự
                    executeDeleteRoom(room);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openRoom(int roomId, String roomName) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("maPhong", roomId);
        intent.putExtra("roomName", roomName);
        startActivity(intent);
    }

    private void openPrivateChat(int friendId, String friendName) {
        String url = Constants.BASE_URL + "chat/private/" + friendId;
        SessionManager sm = new SessionManager(requireContext());

        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    try {
                        JSONObject obj = new JSONObject(resp);
                        int roomId = obj.optInt("roomId", -1);
                        if (roomId > 0) {
                            openRoom(roomId, friendName);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "openPrivateChat parse error", e);
                    }
                },
                err -> Log.e(TAG, "openPrivateChat error: " + err)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("myId", String.valueOf(sm.getMaTaiKhoan()));
                return p;
            }
        };

        Volley.newRequestQueue(requireContext()).add(req);
    }

    private void searchRooms(String keyword, int myId) {
        String url = Constants.BASE_URL + "chat/search?myId=" + myId + "&keyword=" + keyword;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        if (!res.optString("status").equals("success")) return;
                        JSONArray arr = res.optJSONArray("results");
                        if (arr == null) return;

                        searchResults.clear();
                        java.util.HashSet<String> seen = new java.util.HashSet<>();
                        ArrayList<JSONObject> roomOnly = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String type = obj.optString("type", "room");

                            // Chỉ lấy kết quả phòng chat
                            if (!"room".equals(type)) {
                                continue;
                            }

                            // Dedupe theo id phòng
                            String key = "room:" + obj.optInt("id", -1);
                            if (seen.contains(key)) {
                                continue;
                            }
                            seen.add(key);
                            roomOnly.add(obj);
                        }

                        searchResults.addAll(roomOnly);

                        // Dùng FriendAdapter để hiển thị avatar + tên cho phòng
                        listViewPhongchat.setAdapter(new com.example.chatrealtime.adapter.FriendAdapter(requireContext(), roomOnly));
                    } catch (Exception e) {
                        Log.e(TAG, "searchRooms parse error", e);
                    }
                },
                err -> Log.e(TAG, "searchRooms error: " + err)
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    // Gọi API xóa phòng và cập nhật giao diện + SQLite
    private void executeDeleteRoom(Room room) {
        String url = Constants.BASE_URL + "chat/delete-room";
        SessionManager session = new SessionManager(requireContext());
        String token = session.getToken();
        int maTaiKhoan = session.getMaTaiKhoan();

        // Hiển thị loading nhẹ (nếu muốn)
        // ProgressDialog pd = new ProgressDialog(requireContext());
        // pd.setMessage("Đang xóa...");
        // pd.show();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // if (pd.isShowing()) pd.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            // 1. Xóa khỏi danh sách hiển thị (RAM)
                            roomList.remove(room);
                            roomAdapter.notifyDataSetChanged();

                            // 2. Xóa khỏi SQLite (Offline)
                            if (dbHelper != null) {
                                dbHelper.deleteRoom(room.getId());
                            }

                            Toast.makeText(requireContext(), "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi parse JSON xóa phòng: " + e.getMessage());
                    }
                },
                error -> {
                    // if (pd.isShowing()) pd.dismiss();
                    Log.e(TAG, "Lỗi API xóa phòng: " + error.toString());
                    Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Gửi ID phòng cần xóa
                params.put("maPhongChat", String.valueOf(room.getId()));
                // Gửi ID người xóa để server kiểm tra quyền (nếu cần)
                params.put("maTaiKhoan", String.valueOf(maTaiKhoan));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    // Cập nhật tin nhắn mới trong danh sách phòng
    private void updateRoomWithNewMessage(int maPhong, String noiDung, int senderId) {
        SessionManager session = new SessionManager(requireContext());
        int myId = session.getMaTaiKhoan();

        boolean found = false;
        for (int i = 0; i < roomList.size(); i++) {
            // Cập nhật nội dung tin nhắn cuối
            Room r = roomList.get(i);

            if (r.getId() == maPhong) {
                r.setLastMessage(noiDung);

                // LOGIC QUYẾT ĐỊNH HIỆN THỊ TIN NHẮN CHƯA ĐỌC:
                // 1. Người gửi KHÔNG PHẢI là mình (senderId != myId)
                // 2. Mình đang KHÔNG mở phòng chat đó (ChatActivity.CURRENT_OPEN_ROOM != maPhong)
                if (senderId != myId && ChatActivity.CURRENT_OPEN_ROOM != maPhong) {
                    r.incrementUnread();
                }
                // Đưa phòng lên đầu
                Room top = roomList.remove(i);
                roomList.add(0, top);

                found = true;
                break;
            }
        }

        if (found) {
            roomAdapter.notifyDataSetChanged(); // Cập nhật giao diện ngay lập tức
            // Lưu lại để không mất unread khi rời fragment
            dbHelper.saveRooms(roomList);
        } else {
            // Không có trong danh sách (có thể đã xóa): tạo tạm để hiển thị unread đầu tiên
            if (senderId != myId && ChatActivity.CURRENT_OPEN_ROOM != maPhong) {
                Room temp = new Room(maPhong, "Đoạn chat", noiDung, "");
                temp.setUnreadCount(1);
                roomList.add(0, temp);
                roomAdapter.notifyDataSetChanged();
                dbHelper.saveRooms(roomList);
            }
            // Đồng thời gọi API để đồng bộ tên/ảnh chính xác
            loadRoomsFromApi();
        }
    }

    // API lấy danh sách phòng chat
    private void loadRoomsFromApi() {
        SessionManager session = new SessionManager(requireContext());
        int maTaiKhoan = session.getMaTaiKhoan();

        String url = Constants.BASE_URL +  "chat/rooms?maTaiKhoan=" + maTaiKhoan + "&hideDeletedGroupsWithoutNewMessage=true";
        Log.d(TAG, " Gọi API: " + url);

        String token = session.getToken();

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response);
                        if (response.getString("status").equals("success")) {
                            JSONArray rooms = response.getJSONArray("rooms");
                            // Giữ lại unread cục bộ (đã lưu vào DB) để tránh bị reset do độ trễ server
                            Map<Integer, Integer> currentUnread = new HashMap<>();
                            for (Room r : roomList) {
                                currentUnread.put(r.getId(), r.getUnreadCount());
                            }
                            roomList.clear();
                            for (int i = 0; i < rooms.length(); i++) {
                                JSONObject r = rooms.getJSONObject(i);

                                // Phải lấy đúng key "id" chứ không phải "maPhong"
                                int id = r.optInt("id", -1);
                                String name = r.optString("tenPhongChat", "Không rõ");
                                String lastMsg = r.optString("lastMessage", "(Chưa có tin nhắn)");
                                int unread = r.optInt("unreadCount", 0);
                                unread = Math.max(unread, currentUnread.getOrDefault(id, 0));
                                String avatarUrlRaw = r.optString("anhDaiDien_URL", "");
                                String avatarUrl = normalizeAvatarUrl(avatarUrlRaw);

                                Room room = new Room(id, name, lastMsg, avatarUrl);
                                room.setUnreadCount(unread);
                                roomList.add(room);
                            }
                            roomAdapter.notifyDataSetChanged();
                            dbHelper.saveRooms(roomList);
                        } else {
                            Toast.makeText(requireContext(),
                                    response.optString("message", "Lỗi tải danh sách phòng"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "API error: " + error);
                    Toast.makeText(requireContext(), "Không tải được phòng chat", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        queue.add(request);
    }

    // Chuẩn hóa avatar để tránh Glide load "/null"
    private String normalizeAvatarUrl(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "/null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("/")) {
            return Constants.IMAGE_BASE_URL + trimmed;
        }
        return trimmed;
    }

    /**
     * (MỚI) Gọi API để đánh dấu đã đọc trên Server
     * Giúp khi load lại app không bị hiện lại số đỏ
     */
    private void markAsReadOnServer(int maPhong) {
        SessionManager session = new SessionManager(requireContext());
        String url = Constants.BASE_URL +  "chat/mark-read";

        Log.d(TAG, "Calling mark_read for room: " + maPhong);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d(TAG, "Server marked read: " + response),
                error -> Log.e(TAG, "Error marking read: " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Gửi mã phòng lên để server update trạng thái thành 'read'
                params.put("maPhongChat", String.valueOf(maPhong));
                params.put("maTaiKhoan", String.valueOf(session.getMaTaiKhoan()));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Gửi token để server biết ai đang đọc
                headers.put("Authorization", "Bearer " + session.getToken());
                return headers;
            }
        };

        // Thêm request vào hàng đợi
        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisibleToUser = true;
        // Khi quay lại màn hình danh sách, load lại API để cập nhật số tin chưa đọc chính xác nhất
        loadRoomsFromApi();
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisibleToUser = false;
    }

    private static class SearchItem {
        final int id;
        final String name;
        final String type;

        SearchItem(int id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
    }
}
