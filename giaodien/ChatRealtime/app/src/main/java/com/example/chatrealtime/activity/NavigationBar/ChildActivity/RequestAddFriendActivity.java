package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.adapter.FriendRequestAdapter;
import com.example.chatrealtime.model.FriendRequest;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequestAddFriendActivity extends AppCompatActivity {

    ListView lvFriendRequests;
    List<FriendRequest> requestList;
    FriendRequestAdapter adapter;
    ImageView ivBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_add_friend);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SessionManager sessionManager = new SessionManager(this);
        int maTaiKhoanDangNhap = sessionManager.getMaTaiKhoan();
        lvFriendRequests = findViewById(R.id.lv_request_add_friend);
        requestList = new ArrayList<>();
        adapter = new FriendRequestAdapter(this, requestList, maTaiKhoanDangNhap);

        lvFriendRequests.setAdapter(adapter);

        ivBack = findViewById(R.id.iv_back_request_add_friend);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        WebSocketService.getInstance().getMessageLiveData().observe(this, text -> {
            try {
                JSONObject json = new JSONObject(text);
                String type = json.optString("type", "");
                int fromUser = json.optInt("fromUser", -1);

                switch (type) {
                    case "friend_request":
                        String message = json.optString("message", "");
                        addRealtimeRequest(fromUser, message);
                        break;

                    case "friend_cancel":
                        removeRealtimeRequest(fromUser);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        loadRequests();
    }

    private void removeRealtimeRequest(int fromUserId) {
        for (int i = 0; i < requestList.size(); i++) {
            if (requestList.get(i).getMaTaiKhoanGui() == fromUserId) {
                requestList.remove(i);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Người dùng đã hủy lời mời kết bạn", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }


//    private void addRealtimeRequest(int fromUserId, String message) {
//        String url = Constants.BASE_URL + "user/by-id?maTaiKhoan=" + fromUserId;
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                response -> {
//                    try {
//                        JSONObject obj = response.getJSONObject("data"); // data chứa thông tin người dùng
//                        String tenNguoiGui = obj.optString("tenNguoiDung", "Người dùng");
//                        String email = obj.optString("email", "");
//                        String avatar = obj.optString("anhDaiDien_URL", "default_avatar.png");
//
//                        FriendRequest friendRequest = new FriendRequest(fromUserId, tenNguoiGui, email, avatar);
//                        requestList.add(0, friendRequest);
//                        adapter.notifyDataSetChanged();
//
//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> {
//                    Toast.makeText(this, "Không thể tải thông tin người gửi", Toast.LENGTH_SHORT).show();
//                }
//        );
//
//        Volley.newRequestQueue(this).add(request);
//    }

    private void addRealtimeRequest(int fromUserId, String message) {
        if (fromUserId <= 0 || containsRequestFromUser(fromUserId)) {
            return;
        }

        String url = Constants.BASE_URL + "user/by-id?maTaiKhoan=" + fromUserId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) return;

                        JSONObject obj = response.getJSONObject("data");

                        String tenNguoiGui = obj.optString("tenNguoiDung", "Người dùng");
                        String avatar = obj.optString("anhDaiDien_URL", "");
                        String email = ""; // backend không trả email

                        if (containsRequestFromUser(fromUserId)) {
                            return;
                        }

                        requestList.add(0, new FriendRequest(fromUserId, tenNguoiGui, email, avatar));
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this,
                            com.example.chatrealtime.network.ServerMessageDecoder.normalize(message),
                            Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Không thể tải thông tin người gửi", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }



//    private void loadRequests() {
//        SessionManager sessionManager = new SessionManager(this);
//        int maTaiKhoan = sessionManager.getMaTaiKhoan();
//        Log.e("MA_TAI_KHOAN", String.valueOf(maTaiKhoan));
//
//        String url = Constants.BASE_URL + "user/by-id?maTaiKhoan=" + maTaiKhoan;
//
//        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
//                response -> {
//                    requestList.clear();
//                    for (int i = 0; i < response.length(); i++) {
//                        try {
//                            JSONObject obj = response.getJSONObject(i);
//                            int maNguoiGui = obj.getInt("maNguoiGui");
//                            String tenNguoiGui = obj.optString("tenNguoiGui", "Người dùng");
//                            String emailNguoiGui = obj.optString("emailNguoiGui", "");
//                            String avatarUrl = obj.optString("anhDaiDien_URL", "default_avatar.png");
//
//                            requestList.add(new FriendRequest(maNguoiGui, tenNguoiGui, emailNguoiGui, avatarUrl));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    adapter.notifyDataSetChanged();
//                },
//                error -> Toast.makeText(this, "Không thể tải danh sách lời mời", Toast.LENGTH_SHORT).show()
//        );
//
//        Volley.newRequestQueue(this).add(request);
//    }

    private void loadRequests() {
        int maTaiKhoan = new SessionManager(this).getMaTaiKhoan();

        String url = Constants.BASE_URL + "friends/requests?maTaiKhoan=" + maTaiKhoan;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    requestList.clear();
                    Set<Integer> senderIds = new HashSet<>();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.optJSONObject(i);
                        if (obj == null) continue;

                        int maNguoiGui = obj.optInt("maNguoiGui");
                        if (maNguoiGui <= 0 || senderIds.contains(maNguoiGui)) {
                            continue;
                        }
                        senderIds.add(maNguoiGui);

                        requestList.add(new FriendRequest(
                                maNguoiGui,
                                obj.optString("tenNguoiGui"),
                                obj.optString("emailNguoiGui"),
                                obj.optString("anhDaiDien_URL")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Không thể tải danh sách lời mời", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private boolean containsRequestFromUser(int fromUserId) {
        for (FriendRequest req : requestList) {
            if (req.getMaTaiKhoanGui() == fromUserId) {
                return true;
            }
        }
        return false;
    }



}
