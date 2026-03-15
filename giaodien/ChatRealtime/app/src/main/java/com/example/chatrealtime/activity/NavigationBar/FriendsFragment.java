package com.example.chatrealtime.activity.NavigationBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.AddFriendActivity;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChatActivity;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.CreateGroupActivity;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.RequestAddFriendActivity;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.SuggestFriendsActivity;
import com.example.chatrealtime.adapter.FriendAdapter;
import com.example.chatrealtime.adapter.RoomAdapter;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.Room;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private Button btnBanBe, btnNhom, btnThemBanBe, btnLoiMoiKetBan, btnGoiYKetBan, btnTaoNhom;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList;
    private ChatDatabaseHelper dbHelper;
    private int maTaiKhoan;
    private TextView tvSuggestionHeader;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_friends, container, false);

        btnBanBe = view.findViewById(R.id.btn_banbe);
        btnThemBanBe = view.findViewById(R.id.btn_them_ban_be);
        btnLoiMoiKetBan = view.findViewById(R.id.btn_loi_moi_ket_ban);
        btnNhom = view.findViewById(R.id.btn_nhom);
        btnGoiYKetBan = view.findViewById(R.id.btn_goi_y_ket_ban);
        btnTaoNhom = view.findViewById(R.id.btn_tao_nhom);
        listView = view.findViewById(R.id.list_view);
        tvSuggestionHeader = view.findViewById(R.id.tv_suggestion_header);
        etSearch = view.findViewById(R.id.et_search_friend);

        SessionManager sessionManager = new SessionManager(getContext());
        maTaiKhoan = sessionManager.getMaTaiKhoan();
        Log.d("FriendsFragment", "Mã tài khoản hiện tại: " + maTaiKhoan);

        dbHelper = new ChatDatabaseHelper(getContext());

        // Mặc định hiển thị tab "Bạn bè"
        loadDanhSachBanBe(maTaiKhoan);
        setTabSelected(true);

        // Khi nhấn "Bạn bè"
        btnBanBe.setOnClickListener(v -> {
            // xoa danh sach nhom neu co
            listView.setAdapter(null);
            // load lai danh sach ban be
            loadDanhSachBanBe(maTaiKhoan);
            setTabSelected(true);
        });

        // Khi nhấn "Nhóm"
        btnNhom.setOnClickListener(v -> {
            // xoa danh sach ban be neu co
            listView.setAdapter(null);
            loadDanhSachNhom(maTaiKhoan);
            setTabSelected(false);
        });

        // Khi nhấn "Gợi ý kết bạn"
        btnGoiYKetBan.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SuggestFriendsActivity.class);
            startActivity(intent);
        });

        btnTaoNhom.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateGroupActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện "Thêm bạn bè"
        btnThemBanBe.setOnClickListener(v -> {
            // TODO: mở Activity thêm bạn bè hoặc Fragment mới
            // Ví dụ: startActivity(new Intent(getContext(), AddFriendActivity.class));
            Intent intent = new Intent(getContext(), AddFriendActivity.class);
            startActivity(intent);
        });

        btnLoiMoiKetBan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RequestAddFriendActivity.class);
                startActivity(intent);
            }
        });

        // Lắng nghe WebSocket realtime
        WebSocketService webSocketService = WebSocketService.getInstance();
        webSocketService.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            try {
                JSONObject json = new JSONObject(msg);
                String type = json.optString("type");

                if (type.equals("friend_accepted")) {
                    int toUser = json.optInt("toUser");
                    int fromUser = json.optInt("fromUser");

                    // Nếu người này là 1 trong 2 người liên quan → reload danh sách
                    if (toUser == maTaiKhoan || fromUser == maTaiKhoan) {
                        loadDanhSachBanBe(maTaiKhoan);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Tìm kiếm realtime
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadDanhSachBanBe(maTaiKhoan);
                } else {
                    searchFriendOrRoom(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        return view;
    }

//    private void loadDanhSachBanBe(int maTaiKhoan) {
//        String url = Constants.BASE_URL + "get_friends.php?maTaiKhoan=" + maTaiKhoan;
//
//        ArrayList<JSONObject> friendList = new ArrayList<>();
//
//        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
//                response -> {
//                    try {
//                        for (int i = 0; i < response.length(); i++) {
//                            JSONObject friend = response.getJSONObject(i);
//                            friendList.add(friend);
//                        }
//
//                        if (friendList.isEmpty()) {
//                            JSONObject empty = new JSONObject();
//                            empty.put("hoTen", "Chưa có bạn bè nào.");
//                            friendList.add(empty);
//                        }
//
//                        FriendAdapter adapter = new FriendAdapter(requireContext(), friendList);
//                        listView.setAdapter(adapter);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> {
//                    error.printStackTrace();
//                });
//
//        Volley.newRequestQueue(requireContext()).add(request);
//    }

    //  API lấy danh sách bạn bè và tích hợp Offline
    private void loadDanhSachBanBe(int maTaiKhoan) {
        tvSuggestionHeader.setVisibility(View.GONE);
        etSearch.clearFocus();

        // BƯỚC 1: Load dữ liệu OFFLINE từ SQLite trước (Hiển thị ngay lập tức)
        ArrayList<JSONObject> offlineList = dbHelper.getFriends();
        if (!offlineList.isEmpty()) {
            FriendAdapter offlineAdapter = new FriendAdapter(requireContext(), offlineList);
            listView.setAdapter(offlineAdapter);
        }

        // BƯỚC 2: Gọi API lấy dữ liệu MỚI NHẤT
        String url = Constants.BASE_URL + "friends?maTaiKhoan=" + maTaiKhoan;

        // Tạo danh sách mới để hứng dữ liệu Online
        //ArrayList<JSONObject> onlineList = new ArrayList<>();

//        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
//                response -> {
//                    try {
//                        for (int i = 0; i < response.length(); i++) {
//                            JSONObject friend = response.getJSONObject(i);
//                            onlineList.add(friend);
//                        }
//
//                        // Xử lý trường hợp danh sách rỗng
//                        if (onlineList.isEmpty()) {
//                            JSONObject empty = new JSONObject();
//                            empty.put("hoTen", "Chưa có bạn bè nào.");
//                            onlineList.add(empty);
//                        } else {
//                            // BƯỚC 3: Nếu có dữ liệu thật -> LƯU VÀO SQLITE
//                            // (Chỉ lưu khi danh sách không rỗng để tránh lưu item "Chưa có bạn bè")
//                            dbHelper.saveFriends(onlineList);
//                        }
//
//                        // Cập nhật giao diện với dữ liệu mới nhất từ Server
//                        FriendAdapter adapter = new FriendAdapter(requireContext(), onlineList);
//                        listView.setAdapter(adapter);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> {
//                    error.printStackTrace();
//                    // Nếu lỗi mạng, người dùng vẫn xem được danh sách offline đã load ở Bước 1
//                    // Bạn có thể Toast báo lỗi mạng nếu muốn
//                    // Toast.makeText(requireContext(), "Không thể cập nhật danh sách bạn bè", Toast.LENGTH_SHORT).show();
//                });

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) return;

                        JSONArray arr = response.getJSONArray("friends");
                        ArrayList<JSONObject> onlineList = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            onlineList.add(arr.getJSONObject(i));
                        }

                        if (!onlineList.isEmpty()) {
                            dbHelper.saveFriends(onlineList);
                        }

                        FriendAdapter adapter =
                                new FriendAdapter(requireContext(), onlineList);
                        listView.setAdapter(adapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void searchFriendOrRoom(String keyword) {
        String url = Constants.BASE_URL + "chat/search?myId=" + maTaiKhoan + "&keyword=" + keyword;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.optString("status").equals("success")) return;

                        JSONArray arr = response.optJSONArray("results");
                        if (arr == null) return;

                        ArrayList<JSONObject> searchList = new ArrayList<>();
                        java.util.HashSet<String> seen = new java.util.HashSet<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject item = arr.getJSONObject(i);

                            // Bỏ các phòng 1-1 (loaiPhong=false/0) để không trùng với bản ghi bạn bè
                            String type = item.optString("type", "friend");
                            if ("room".equals(type)) {
                                boolean isGroup;
                                if (item.has("loaiPhong") && item.get("loaiPhong") instanceof Boolean) {
                                    isGroup = item.optBoolean("loaiPhong", false);
                                } else {
                                    isGroup = item.optInt("loaiPhong", 0) != 0;
                                }
                                if (!isGroup) {
                                    continue; // skip phòng 1-1 trong khung tìm kiếm bạn bè
                                }
                            }

                            String key = buildSearchKey(item);
                            if (seen.contains(key)) {
                                continue; // bỏ trùng
                            }
                            seen.add(key);
                            searchList.add(item);
                        }

                        listView.setAdapter(new FriendAdapter(requireContext(), searchList));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("FriendsFragment", "Search error: " + error.toString())
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    private String buildSearchKey(JSONObject item) {
        String type = item.optString("type", "friend");
        if ("room".equals(type)) {
            return type + ":" + item.optInt("id", -1);
        }
        // friend result
        if (item.has("maTaiKhoan")) {
            return type + ":" + item.optInt("maTaiKhoan", -1);
        }
        // fallback tránh null
        return type + ":" + item.optString("tenNguoiDung", "") + ":" + item.optString("tenPhongChat", "") + ":" + item.optString("id", "");
    }



    private void loadDanhSachNhom(int maTaiKhoan) {
        tvSuggestionHeader.setVisibility(View.VISIBLE);
        tvSuggestionHeader.setText("Danh sách nhóm của bạn");

        String url = Constants.BASE_URL + "chat/rooms?maTaiKhoan=" + maTaiKhoan;

        SessionManager session = new SessionManager(requireContext());
        String token = session.getToken();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("FriendsFragment", "Rooms response: " + response);
                        if (!"success".equals(response.optString("status"))) return;
                        JSONArray arr = response.optJSONArray("rooms");
                        Log.d("FriendsFragment", "Total rooms in response: " + (arr != null ? arr.length() : 0));
                        List<Room> groups = new ArrayList<>();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);

                                // Server trả loaiPhong kiểu boolean (true/false) nên phải ép kiểu an toàn
                                boolean isGroup;
                                if (o.has("loaiPhong") && o.get("loaiPhong") instanceof Boolean) {
                                    isGroup = o.optBoolean("loaiPhong", false);
                                } else {
                                    int loaiPhongInt = o.optInt("loaiPhong", 0);
                                    isGroup = loaiPhongInt != 0;
                                }

                                if (!isGroup) continue; // bỏ phòng 1-1

                                int id = o.optInt("id", -1);
                                String tenPhong = o.optString("tenPhongChat", "Nhóm");
                                String lastMsg = o.optString("lastMessage", "");
                                String avatar = normalizeAvatarUrl(o.optString("anhDaiDien_URL", ""));

                                Room r = new Room(id, tenPhong, lastMsg, avatar);
                                r.setUnreadCount(o.optInt("unreadCount", 0));
                                groups.add(r);
                                Log.d("FriendsFragment", "Add group id=" + id + " isGroup=" + isGroup + " name=" + tenPhong);
                            }
                        }

                        Log.d("FriendsFragment", "Groups after filter: " + groups.size());

                        if (groups.isEmpty()) {
                            tvSuggestionHeader.setText("Chưa có nhóm nào, nhấn Tạo nhóm để bắt đầu");
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, java.util.Collections.singletonList("Không có nhóm để hiển thị"));
                            listView.setAdapter(emptyAdapter);
                            Toast.makeText(requireContext(), "Chưa có nhóm nào", Toast.LENGTH_SHORT).show();
                        } else {
                            RoomAdapter adapter = new RoomAdapter(requireContext(), groups);
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener((parent, view1, position, id) -> {
                                Room room = groups.get(position);
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("maPhong", room.getId());
                                intent.putExtra("roomName", room.getName());
                                startActivity(intent);
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> h = new java.util.HashMap<>();
                h.put("Authorization", "Bearer " + token);
                return h;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

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


    private void loadGoiYBanBe(int maTaiKhoan) {
        String url = Constants.BASE_URL + "friends/suggest?maTaiKhoan=" + maTaiKhoan;
        tvSuggestionHeader.setVisibility(View.VISIBLE);
        tvSuggestionHeader.setText("Gợi ý kết bạn (nhấn vào để gửi lời mời)");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!"success".equals(response.optString("status"))) return;
                        JSONArray arr = response.optJSONArray("suggestions");
                        ArrayList<JSONObject> list = new ArrayList<>();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                o.put("suggestion", true);
                                list.add(o);
                            }
                        }
                        listView.setAdapter(new FriendAdapter(requireContext(), list));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

//    // Hàm đổi màu tab theo Zalo-style
//    private void setTabSelected(boolean isFriendTab) {
//        if (isFriendTab) {
//            btnBanBe.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
//            btnBanBe.setTextColor(getResources().getColor(R.color.white));
//
//            btnNhom.setBackgroundTintList(getResources().getColorStateList(R.color.white));
//            btnNhom.setTextColor(getResources().getColor(R.color.black));
//
//            // Hiện nút Thêm bạn bè
//            btnThemBanBe.setVisibility(View.VISIBLE);
//        } else {
//            btnNhom.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
//            btnNhom.setTextColor(getResources().getColor(R.color.white));
//
//            btnBanBe.setBackgroundTintList(getResources().getColorStateList(R.color.white));
//            btnBanBe.setTextColor(getResources().getColor(R.color.black));
//
//            // Ẩn nút Thêm bạn bè
//            btnThemBanBe.setVisibility(View.GONE);
//        }

    private void setTabSelected(boolean isFriendTab) {
        if (isFriendTab) {
            btnBanBe.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            btnBanBe.setTextColor(getResources().getColor(R.color.white));

            btnNhom.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            btnNhom.setTextColor(getResources().getColor(R.color.black));

            // Hiện nút Thêm bạn bè và Lời mời kết bạn
            btnThemBanBe.setVisibility(View.VISIBLE);
            btnLoiMoiKetBan.setVisibility(View.VISIBLE);
            btnGoiYKetBan.setVisibility(View.VISIBLE);
            btnTaoNhom.setVisibility(View.GONE);
            tvSuggestionHeader.setVisibility(View.GONE);

        } else {
            btnNhom.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            btnNhom.setTextColor(getResources().getColor(R.color.white));

            btnBanBe.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            btnBanBe.setTextColor(getResources().getColor(R.color.black));

            // Ẩn nút Thêm bạn bè và Lời mời kết bạn
            btnThemBanBe.setVisibility(View.GONE);
            btnLoiMoiKetBan.setVisibility(View.GONE);
            btnGoiYKetBan.setVisibility(View.GONE);
            btnTaoNhom.setVisibility(View.VISIBLE);
        }
    }
}
