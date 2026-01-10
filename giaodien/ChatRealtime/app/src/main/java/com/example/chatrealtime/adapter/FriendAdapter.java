package com.example.chatrealtime.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChatActivity;
import com.example.chatrealtime.model.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendAdapter extends BaseAdapter {
    private static final String TAG = "FriendAdapter";
    private final Context context;
    private final ArrayList<JSONObject> friendList;

    public FriendAdapter(Context context, ArrayList<JSONObject> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_friend, parent, false);
            holder = new ViewHolder();
            holder.ivFriendAvatar = convertView.findViewById(R.id.ivFriendAvatar);
            holder.tvFriendName = convertView.findViewById(R.id.tvFriendName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject friend = friendList.get(position);
        Log.d(TAG, "Hiển thị bạn bè tại vị trí: " + position + " → " + friend);

//        String name = friend.optString("tenNguoiDung", "Không rõ");
//        holder.tvFriendName.setText(name.isEmpty() ? "Không rõ" : name);
//
//        String avatarUrl = friend.optString("anhDaiDien_URL", "");
//        if (avatarUrl != null && !avatarUrl.isEmpty()) {
//            Glide.with(context)
//                    .load(avatarUrl)
//                    .placeholder(R.drawable.avatar_default)
//                    .error(R.drawable.avatar_default)
//                    .into(holder.ivFriendAvatar);
//        } else {
//            holder.ivFriendAvatar.setImageResource(R.drawable.avatar_default);
//        }

        String name = friend.optString("tenNguoiDung", "").trim();

        if (name.isEmpty()) {
            // Ẩn cả avatar lẫn tên
            //holder.tvFriendName.setVisibility(View.GONE);
            //holder.ivFriendAvatar.setVisibility(View.GONE);

            // Nếu muốn ẩn toàn bộ item luôn:
            convertView.setVisibility(View.GONE);
        } else {
            holder.tvFriendName.setVisibility(View.VISIBLE);
            holder.tvFriendName.setText(name);

            holder.ivFriendAvatar.setVisibility(View.VISIBLE);
//            String avatarUrl = friend.optString("anhDaiDien_URL", "");
//            if (avatarUrl != null && !avatarUrl.isEmpty()) {
//                Glide.with(context)
//                        .load(avatarUrl)
//                        .placeholder(R.drawable.avatar_default)
//                        .error(R.drawable.avatar_default)
//                        .into(holder.ivFriendAvatar);
//            } else {
//                holder.ivFriendAvatar.setImageResource(R.drawable.avatar_default);
//            }

            String avatarPath = friend.optString("anhDaiDien_URL", "");

            if (!avatarPath.isEmpty()) {
                String fullUrl;

                if (avatarPath.startsWith("http")) {
                    fullUrl = avatarPath;
                } else {
                    fullUrl = Constants.IMAGE_BASE_URL + avatarPath;
                }

                Glide.with(context)
                        .load(fullUrl)
                        .placeholder(R.drawable.avatar_default)
                        .error(R.drawable.avatar_default)
                        .into(holder.ivFriendAvatar);
            } else {
                holder.ivFriendAvatar.setImageResource(R.drawable.avatar_default);
            }

        }

        convertView.setOnClickListener(v -> handleFriendClick(friend, name));

        return convertView;
    }

    // ✅ Khi click vào bạn bè
    private void handleFriendClick(JSONObject friend, String friendName) {
        try {
            int friendId = friend.getInt("maTaiKhoan");
            SessionManager sm = new SessionManager(context);
//            String token = sm.getToken();
//
//            if (token == null || token.isEmpty()) {
//                Toast.makeText(context, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
//                return;
//            }

            Log.d(TAG, "Click bạn bè → friendId=" + friendId);

            String url = Constants.BASE_URL + "chat/create-room";
            Log.d(TAG, "Gửi request: " + url);

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    response -> {
                        Log.d(TAG, "Phản hồi từ server: " + response);
                        try {
                            JSONObject obj = new JSONObject(response);

                            // ✅ Nếu có mã phòng → vào phòng
                            if (obj.has("maPhongChat")) {
                                int maPhong = obj.getInt("maPhongChat");
                                Log.i(TAG, "✅ Vào phòng có sẵn → maPhong=" + maPhong);
                                openChat(maPhong, friendId, friendName);
                                return;
                            }

                            // ✅ Nếu có danh sách rooms → vào phòng đầu tiên
                            if (obj.has("rooms")) {
                                JSONArray rooms = obj.getJSONArray("rooms");
                                if (rooms.length() > 0) {
                                    JSONObject firstRoom = rooms.getJSONObject(0);
                                    int maPhong = firstRoom.getInt("maPhongChat");
                                    Log.i(TAG, "✅ Đã có phòng → maPhong=" + maPhong);
                                    openChat(maPhong, friendId, friendName);
                                } else {
                                    Log.i(TAG, "⚙️ Chưa có phòng → tạo mới");
                                    createNewRoom(friendId, friendName);
                                }
                            } else {
                                Log.i(TAG, "⚙️ Không có rooms → tạo mới");
                                createNewRoom(friendId, friendName);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Lỗi xử lý phản hồi server: " + e.getMessage(), e);
                            Toast.makeText(context, "Lỗi phản hồi server", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e(TAG, "❌ Lỗi kết nối server: " + error.toString());
                        Toast.makeText(context, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                    }
            ) {
//                @Override
//                protected Map<String, String> getParams() {
//                    Map<String, String> params = new HashMap<>();
//                    // ✅ Backend chỉ cần friendId, userId lấy từ token
//                    params.put("members", "[" + friendId + "]");
//                    return params;
//                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("members", String.valueOf(friendId)); // 1-1 chat
                    params.put("currentUserId", String.valueOf(
                            new SessionManager(context).getMaTaiKhoan()
                    ));
                    return params;
                }


//                @Override
//                public Map<String, String> getHeaders() {
//                    Map<String, String> headers = new HashMap<>();
//                    headers.put("Authorization", "Bearer " + token);
//                    return headers;
//                }
            };

            Volley.newRequestQueue(context).add(req);

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi khi click bạn bè: " + e.getMessage(), e);
        }
    }

    // ✅ Tạo phòng mới khi chưa có
    private void createNewRoom(int friendId, String friendName) {
        String url = Constants.BASE_URL + "chat/create-room";
        SessionManager sm = new SessionManager(context);
        String token = sm.getToken();

        Log.d(TAG, "🔨 Gửi request tạo phòng mới: " + url);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Phản hồi khi tạo phòng mới: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success") && obj.has("maPhongChat")) {
                            int maPhong = obj.getInt("maPhongChat");
                            Log.i(TAG, "✅ Tạo phòng mới thành công → maPhong=" + maPhong);
                            openChat(maPhong, friendId, friendName);
                        } else {
                            Toast.makeText(context, "Không thể tạo phòng mới", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Lỗi khi tạo phòng mới: " + e.getMessage(), e);
                    }
                },
                error -> Log.e(TAG, "❌ Lỗi kết nối khi tạo phòng mới: " + error)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("members", "[" + friendId + "]");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(req);
    }

    // ✅ Mở phòng chat
    private void openChat(int maPhong, int friendId, String friendName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("maPhong", maPhong);
        intent.putExtra("friendId", friendId);
        intent.putExtra("friendName", friendName);
        context.startActivity(intent);
    }

    static class ViewHolder {
        ImageView ivFriendAvatar;
        TextView tvFriendName;
    }
}
