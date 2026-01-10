package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MenuChatActivity extends AppCompatActivity {

    LinearLayout layoutTrangCaNhan;
    TextView tvTenPhong;
    ImageView imgBack;
    ShapeableImageView imgAvatarRoom;

    private int friendId = -1;

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

        int maPhong = getIntent().getIntExtra("maPhong", -1);
        if (maPhong == -1) {
            Toast.makeText(this, "Không tìm thấy phòng chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String tenPhong = getIntent().getStringExtra("roomName");

        layoutTrangCaNhan = findViewById(R.id.lnlayout_trangcanhan);
        tvTenPhong = findViewById(R.id.tv_tenphong);
        imgBack = findViewById(R.id.iv_back_menu_chat);
        imgAvatarRoom = findViewById(R.id.iv_menu_avatar);

        tvTenPhong.setText(tenPhong);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        loadRoomMembers(maPhong);

        layoutTrangCaNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuChatActivity.this, PersionalPageActivity.class);
                intent.putExtra("friendId", friendId);
                startActivity(intent);
            }
        });

    }

    private void loadRoomMembers(int maPhong) {
        String url = Constants.BASE_URL + "get_room_member.php?maPhongChat=" + maPhong;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray arr = response.getJSONArray("members");

                            SessionManager session = new SessionManager(this);
                            int myId = session.getMaTaiKhoan();

                            // Lấy người còn lại
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                int id = obj.getInt("maTaiKhoan");

                                if (id != myId) {
                                    friendId = id; // lưu lại để mở trang cá nhân
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) { }
                },
                error -> {}
        );

        queue.add(request);
    }
}