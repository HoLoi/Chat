package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.adapter.SuggestFriendAdapter;
import com.example.chatrealtime.model.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SuggestFriendsActivity extends AppCompatActivity {

    private ListView lvSuggest;
    private TextView tvEmpty;
    private ArrayList<JSONObject> data = new ArrayList<>();
    private SuggestFriendAdapter adapter;
    private int myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggest_friends);

        lvSuggest = findViewById(R.id.lvSuggest);
        tvEmpty = findViewById(R.id.tvEmpty);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        myId = new SessionManager(this).getMaTaiKhoan();
        adapter = new SuggestFriendAdapter(this, data);
        lvSuggest.setAdapter(adapter);

        loadSuggestions();
    }

    private void loadSuggestions() {
        String url = Constants.BASE_URL + "friends/suggest?maTaiKhoan=" + myId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!"success".equals(response.optString("status"))) {
                            Toast.makeText(this, "Không tải được gợi ý", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = response.optJSONArray("suggestions");
                        data.clear();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                data.add(obj);
                                // lấy trạng thái kết bạn từ backend cho từng gợi ý
                                fetchStatus(i, obj.optInt("maTaiKhoan", -1));
                            }
                        }

                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // Gọi API trạng thái kết bạn để nút hiển thị đúng (chờ, đã là bạn, chưa kết bạn)
    private void fetchStatus(int index, int friendId) {
        if (friendId <= 0 || index >= data.size()) return;

        String url = Constants.BASE_URL + "friends/status?myId=" + myId + "&friendId=" + friendId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    try {
                        String status = resp.optString("status", "");
                        JSONObject obj = data.get(index);
                        obj.put("relationStatus", status);
                        adapter.notifyDataSetChanged();
                    } catch (Exception ignored) {}
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(req);
    }
}
