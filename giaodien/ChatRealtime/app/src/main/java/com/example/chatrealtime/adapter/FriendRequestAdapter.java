package com.example.chatrealtime.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.FriendRequest;
import com.example.chatrealtime.model.WebSocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestAdapter extends ArrayAdapter<FriendRequest> {
    private Context context;
    private List<FriendRequest> list;
    private int maTaiKhoanDangNhap; // 👈 tài khoản đang đăng nhập

    public FriendRequestAdapter(@NonNull Context context, List<FriendRequest> list, int maTaiKhoanDangNhap) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
        this.maTaiKhoanDangNhap = maTaiKhoanDangNhap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        }

        FriendRequest req = list.get(position);

        TextView tvName = convertView.findViewById(R.id.tvRequestName);
        Button btnAccept = convertView.findViewById(R.id.btnAccept);
        Button btnReject = convertView.findViewById(R.id.btnReject);

        tvName.setText(req.getTenNguoiGui());

        btnAccept.setOnClickListener(v -> handleRequest(req.getMaTaiKhoanGui(), "chap_nhan", position));
        btnReject.setOnClickListener(v -> handleRequest(req.getMaTaiKhoanGui(), "tu_choi", position));

        return convertView;
    }

    private void handleRequest(int maTaiKhoanGui, String action, int position) {
        String url = Constants.BASE_URL + "friends/respond";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        // rename biến phản hồi để tránh trùng tên
                        JSONObject respJson = new JSONObject(response);
                        String status = respJson.getString("status");
                        String message = respJson.getString("message");

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                        if (status.equals("success")) {
                            list.remove(position);
                            notifyDataSetChanged();

                            // ✅ Gửi realtime qua WebSocket khi đồng ý kết bạn
                            if (action.equals("chap_nhan")) {
                                try {
                                    // dùng tên khác để tránh trùng với respJson
                                    JSONObject notifyJson = new JSONObject();
                                    notifyJson.put("type", "friend_accepted");
                                    notifyJson.put("fromUser", maTaiKhoanDangNhap);
                                    notifyJson.put("toUser", maTaiKhoanGui);

                                    WebSocketService.getInstance().sendJson(notifyJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (JSONException e) {
                        Toast.makeText(context, "Lỗi xử lý dữ liệu JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("maTaiKhoan1", String.valueOf(maTaiKhoanGui)); // người gửi lời mời
                params.put("maTaiKhoan2", String.valueOf(maTaiKhoanDangNhap)); // người nhận (đang đăng nhập)
                params.put("action", action);
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }
}
