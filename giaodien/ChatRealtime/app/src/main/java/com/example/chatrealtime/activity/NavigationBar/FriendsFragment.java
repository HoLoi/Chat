package com.example.chatrealtime.activity.NavigationBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.RequestAddFriendActivity;
import com.example.chatrealtime.adapter.FriendAdapter;
import com.example.chatrealtime.database.ChatDatabaseHelper;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    private Button btnBanBe, btnNhom, btnThemBanBe, btnLoiMoiKetBan;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dataList;
    private ChatDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_friends, container, false);

        btnBanBe = view.findViewById(R.id.btn_banbe);
        btnThemBanBe = view.findViewById(R.id.btn_them_ban_be);
        btnLoiMoiKetBan = view.findViewById(R.id.btn_loi_moi_ket_ban);
        btnNhom = view.findViewById(R.id.btn_nhom);
        listView = view.findViewById(R.id.list_view);

        SessionManager sessionManager = new SessionManager(getContext());
        int maTaiKhoan = sessionManager.getMaTaiKhoan();
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
            // load lai danh sach nhom
            loadDanhSachNhom();
            setTabSelected(false);
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

    // ✅ API lấy danh sách bạn bè và tích hợp Offline
    private void loadDanhSachBanBe(int maTaiKhoan) {
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



    private void loadDanhSachNhom() {
        dataList = new ArrayList<>();
        dataList.add("Nhóm lớp 12A");
        dataList.add("CLB Android Dev");
        dataList.add("Đội bóng DNC");

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
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

        } else {
            btnNhom.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            btnNhom.setTextColor(getResources().getColor(R.color.white));

            btnBanBe.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            btnBanBe.setTextColor(getResources().getColor(R.color.black));

            // Ẩn nút Thêm bạn bè và Lời mời kết bạn
            btnThemBanBe.setVisibility(View.GONE);
            btnLoiMoiKetBan.setVisibility(View.GONE);
        }
    }
}
