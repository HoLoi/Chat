package com.example.chatrealtime.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter hiển thị gợi ý kết bạn với nút hành động (kết bạn / hủy lời mời).
 */
public class SuggestFriendAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<JSONObject> users;
    private final int myId;

    public SuggestFriendAdapter(Context context, ArrayList<JSONObject> users) {
        this.context = context;
        this.users = users;
        this.myId = new SessionManager(context).getMaTaiKhoan();
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_suggest_friend, parent, false);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.ivAvatar);
            holder.tvName = convertView.findViewById(R.id.tvName);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.btnAction = convertView.findViewById(R.id.btnAction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject user = users.get(position);
        String name = user.optString("tenNguoiDung", "Người dùng");
        holder.tvName.setText(name);

        String avatar = user.optString("anhDaiDien_URL", "");
        String full = normalizeImageUrl(avatar);
        if (!full.isEmpty()) {
            Glide.with(context).load(full).placeholder(R.drawable.avatar_default).error(R.drawable.avatar_default).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.avatar_default);
        }

        String relation = resolveStatus(user);
        bindAction(holder, user, relation, position);

        return convertView;
    }

    private void bindAction(ViewHolder holder, JSONObject user, String relation, int position) {
        holder.btnAction.setEnabled(true);
        holder.btnAction.setBackgroundTintList(context.getColorStateList(R.color.blue));

        switch (relation) {
            case "friend":
                holder.tvStatus.setText("Đã là bạn");
                holder.btnAction.setText("Bạn bè");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(context.getColor(android.R.color.darker_gray)));
                break;
            case "pending":
            case "cho":
                holder.tvStatus.setText("Đang chờ phản hồi");
                holder.btnAction.setText("Hủy lời mời");
                holder.btnAction.setOnClickListener(v -> confirmCancel(user, position));
                break;
            default:
                holder.tvStatus.setText("Gợi ý kết bạn");
                holder.btnAction.setText("Kết bạn");
                holder.btnAction.setOnClickListener(v -> confirmSend(user, position));
                break;
        }
    }

    private void confirmSend(JSONObject user, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Kết bạn")
                .setMessage("Bạn có muốn gửi lời mời kết bạn?")
                .setPositiveButton("Gửi", (d, w) -> sendFriendRequest(user, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmCancel(JSONObject user, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Hủy lời mời")
                .setMessage("Bạn có muốn hủy lời mời đã gửi?")
                .setPositiveButton("Hủy lời mời", (d, w) -> cancelFriendRequest(user, position))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void sendFriendRequest(JSONObject user, int position) {
        try {
            int friendId = user.getInt("maTaiKhoan");
            String url = Constants.BASE_URL + "friends/send-request";

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    resp -> {
                        Toast.makeText(context, "Đã gửi lời mời", Toast.LENGTH_SHORT).show();
                        try {
                            user.put("relationStatus", "pending");
                            notifyDataSetChanged();
                        } catch (Exception ignored) {}
                    },
                    err -> Toast.makeText(context, "Gửi lời mời thất bại", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("myId", String.valueOf(myId));
                    params.put("friendId", String.valueOf(friendId));
                    return params;
                }
            };

            Volley.newRequestQueue(context).add(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelFriendRequest(JSONObject user, int position) {
        try {
            int friendId = user.getInt("maTaiKhoan");
            String url = Constants.BASE_URL + "friends/cancel-request";

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    resp -> {
                        Toast.makeText(context, "Đã hủy lời mời", Toast.LENGTH_SHORT).show();
                        try {
                            user.put("relationStatus", "not_friend");
                            notifyDataSetChanged();
                        } catch (Exception ignored) {}
                    },
                    err -> Toast.makeText(context, "Hủy lời mời thất bại", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("myId", String.valueOf(myId));
                    params.put("friendId", String.valueOf(friendId));
                    return params;
                }
            };

            Volley.newRequestQueue(context).add(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resolveStatus(JSONObject user) {
        String status = user.optString("relationStatus", "");
        if (status.isEmpty()) status = user.optString("friendStatus", "");
        if (status.isEmpty()) status = user.optString("trangThaiBanBe", "");
        if (status.isEmpty()) status = user.optString("trangThai", "");
        if (status.isEmpty() && user.optBoolean("daKetBan", false)) status = "friend";
        if (status.isEmpty() && user.optBoolean("pending", false)) status = "pending";
        if (status.equalsIgnoreCase("dongy")) status = "friend";
        if (status.equalsIgnoreCase("cho")) status = "pending";
        if (status.isEmpty()) status = "not_friend";
        return status;
    }

    private String normalizeImageUrl(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "/null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("http")) return trimmed;
        if (trimmed.startsWith("/")) return Constants.IMAGE_BASE_URL + trimmed;
        return Constants.IMAGE_BASE_URL + "/" + trimmed;
    }

    static class ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvName;
        TextView tvStatus;
        Button btnAction;
    }
}
