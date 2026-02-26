package com.example.chatrealtime.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChatActivity;
import com.example.chatrealtime.model.Room;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class RoomAdapter extends BaseAdapter {
    private Context context;
    private List<Room> roomList;
    private SessionManager sessionManager;

    public RoomAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public int getCount() {
        return roomList != null ? roomList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
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
                    .inflate(R.layout.item_room, parent, false);

            holder = new ViewHolder();
            holder.txtRoomName = convertView.findViewById(R.id.txtRoomName);
            holder.txtLastMessage = convertView.findViewById(R.id.txtLastMessage);
            holder.txtUnread = convertView.findViewById(R.id.txtUnread);
            holder.ivRoomAvatar = convertView.findViewById(R.id.ivRoomAvatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Room room = roomList.get(position);
        holder.txtRoomName.setText(room.getName());
        holder.txtLastMessage.setText(room.getLastMessage());

//        String avatarUrl = room.getImageUrl();
//        if (avatarUrl != null && !avatarUrl.isEmpty()) {
//            Glide.with(context)
//                    .load(avatarUrl)
//                    .placeholder(R.drawable.avatar_default)
//                    .error(R.drawable.avatar_default)
//                    .into(holder.ivRoomAvatar);
//        } else {
//            holder.ivRoomAvatar.setImageResource(R.drawable.avatar_default);
//        }

        String avatarUrl = normalizeAvatar(room.getImageUrl());

        if (!avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .into(holder.ivRoomAvatar);
        } else {
            holder.ivRoomAvatar.setImageResource(R.drawable.avatar_default);
        }


        // Hiển thị số tin chưa đọc (nếu có)
        if (room.getUnreadCount() > 0) {
            holder.txtUnread.setVisibility(View.VISIBLE);
            holder.txtUnread.setText(String.valueOf(room.getUnreadCount()));
        } else {
            holder.txtUnread.setVisibility(View.GONE);
        }

        // Khi nhấn vào 1 phòng -> mở ChatActivity
//        convertView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ChatActivity.class);
//            intent.putExtra("maPhong", room.getId());
//            intent.putExtra("roomName", room.getName());
//
//            // Có thể truyền token nếu cần xác thực khi mở ChatActivity
//            intent.putExtra("token", sessionManager.getToken());
//            intent.putExtra("email", sessionManager.getEmail());
//            intent.putExtra("maTaiKhoan", sessionManager.getMaTaiKhoan());
//
//            // Reset tin chưa đọc và cập nhật giao diện
//            room.resetUnread();
//            notifyDataSetChanged();
//
//            context.startActivity(intent);
//        });

        return convertView;
    }

    static class ViewHolder {
        TextView txtRoomName, txtLastMessage, txtUnread;
        ShapeableImageView ivRoomAvatar;
    }

    private String normalizeAvatar(String raw) {
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
}
