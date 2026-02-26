package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.adapter.MultiSelectFriendAdapter;
import com.example.chatrealtime.adapter.MultiSelectFriendAdapter.FriendChoice;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChatActivity;
import com.example.chatrealtime.model.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText edtGroupName;
    private Switch switchPrivacy;
    private ListView lvFriends;
    private MultiSelectFriendAdapter adapter;
    private final List<FriendChoice> friendChoices = new ArrayList<>();
    private int myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_group);

        edtGroupName = findViewById(R.id.edtGroupName);
        switchPrivacy = findViewById(R.id.switchPrivacy);
        lvFriends = findViewById(R.id.lvFriends);
        Button btnCreate = findViewById(R.id.btnCreate);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvPrivacy = findViewById(R.id.tvPrivacy);

        myId = new SessionManager(this).getMaTaiKhoan();

        adapter = new MultiSelectFriendAdapter(this, friendChoices);
        lvFriends.setAdapter(adapter);

        switchPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvPrivacy.setText(isChecked ? "Private" : "Public");
        });

        btnBack.setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> createGroup());

        loadFriends();
    }

    private void loadFriends() {
        String url = Constants.BASE_URL + "friends?maTaiKhoan=" + myId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!"success".equals(response.optString("status"))) return;
                        JSONArray arr = response.optJSONArray("friends");
                        friendChoices.clear();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                int id = o.optInt("maTaiKhoan", -1);
                                String name = o.optString("tenNguoiDung", "");
                                String avatar = o.optString("anhDaiDien_URL", "");
                                if (id != -1) {
                                    friendChoices.add(new FriendChoice(id, name, avatar));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Không tải được danh sách bạn bè", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void createGroup() {
        String name = edtGroupName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Nhập tên nhóm", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Integer> selected = adapter.getSelectedIds();
        if (selected.size() < 2) {
            Toast.makeText(this, "Cần chọn tối thiểu 2 thành viên (cùng bạn là 3)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selected.size() > 99) {
            Toast.makeText(this, "Chỉ chọn tối đa 99 thành viên (bạn sẽ được thêm tự động)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo chuỗi thành viên, Spring có thể parse list từ chuỗi comma
        String members = TextUtils.join(",", selected);
        int kieuNhom = switchPrivacy.isChecked() ? 1 : 0;

        String url = Constants.BASE_URL + "chat/create-room";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                resp -> {
                    try {
                        JSONObject obj = new JSONObject(resp);
                        if (obj.has("maPhongChat")) {
                            Toast.makeText(this, "Tạo nhóm thành công", Toast.LENGTH_SHORT).show();
                            int roomId = obj.getInt("maPhongChat");
                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.putExtra("maPhong", roomId);
                            intent.putExtra("roomName", name);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Không tạo được nhóm", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Lỗi tạo nhóm", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tenPhong", name);
                params.put("members", members);
                params.put("currentUserId", String.valueOf(myId));
                params.put("kieuNhom", String.valueOf(kieuNhom));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
