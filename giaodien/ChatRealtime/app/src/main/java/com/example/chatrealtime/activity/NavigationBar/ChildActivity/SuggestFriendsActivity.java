package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private EditText edtSearch;
    private ImageButton btnSearch;
    private final ArrayList<JSONObject> data = new ArrayList<>();
    private final ArrayList<JSONObject> allSuggestions = new ArrayList<>();
    private SuggestFriendAdapter adapter;
    private int myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggest_friends);

        lvSuggest = findViewById(R.id.lvSuggest);
        tvEmpty = findViewById(R.id.tvEmpty);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> runSearch());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearch();
                return true;
            }
            return false;
        });

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
                        allSuggestions.clear();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                allSuggestions.add(obj);
                                // lấy trạng thái kết bạn từ backend cho từng gợi ý
                                fetchStatus(obj, obj.optInt("maTaiKhoan", -1));
                            }
                        }

                        applySearch(edtSearch.getText().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // Gọi API trạng thái kết bạn để nút hiển thị đúng (chờ, đã là bạn, chưa kết bạn)
    private void fetchStatus(JSONObject targetUser, int friendId) {
        if (friendId <= 0 || targetUser == null) return;

        String url = Constants.BASE_URL + "friends/status?myId=" + myId + "&friendId=" + friendId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    try {
                        String status = resp.optString("status", "");
                        targetUser.put("relationStatus", status);
                        adapter.notifyDataSetChanged();
                    } catch (Exception ignored) {}
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void runSearch() {
        applySearch(edtSearch.getText().toString());
    }

    private void applySearch(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();

        data.clear();
        if (TextUtils.isEmpty(normalized)) {
            data.addAll(allSuggestions);
            adapter.notifyDataSetChanged();
            showEmptyState(data.isEmpty(), false);
            return;
        }

        for (JSONObject user : allSuggestions) {
            String displayName = user.optString("tenNguoiDung", "").toLowerCase();
            String username = resolveUsername(user).toLowerCase();

            if (displayName.contains(normalized) || username.contains(normalized)) {
                data.add(user);
            }
        }

        adapter.notifyDataSetChanged();
        showEmptyState(data.isEmpty(), true);
    }

    private void showEmptyState(boolean isEmpty, boolean isSearching) {
        if (isSearching) {
            tvEmpty.setText("Không tìm thấy người dùng");
        } else {
            tvEmpty.setText("Không có gợi ý kết bạn");
        }
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private String resolveUsername(JSONObject user) {
        String username = user.optString("username", "");
        if (username.isEmpty()) username = user.optString("tenDangNhap", "");
        if (username.isEmpty()) username = user.optString("taiKhoan", "");
        if (username.isEmpty()) username = user.optString("email", "");
        return username;
    }
}
