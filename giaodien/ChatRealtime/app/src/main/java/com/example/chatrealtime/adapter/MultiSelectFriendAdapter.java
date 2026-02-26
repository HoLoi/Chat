package com.example.chatrealtime.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectFriendAdapter extends BaseAdapter {

    public static class FriendChoice {
        public int id;
        public String name;
        public String avatar;
        public boolean selected;

        public FriendChoice(int id, String name, String avatar) {
            this.id = id;
            this.name = name;
            this.avatar = avatar;
        }
    }

    private final Context context;
    private final List<FriendChoice> friends;

    public MultiSelectFriendAdapter(Context context, List<FriendChoice> friends) {
        this.context = context;
        this.friends = friends;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_select_friend, parent, false);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.ivAvatar);
            holder.tvName = convertView.findViewById(R.id.tvName);
            holder.cbSelect = convertView.findViewById(R.id.cbSelect);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FriendChoice f = friends.get(position);
        holder.tvName.setText(f.name);
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(f.selected);

        String avatarPath = f.avatar != null ? f.avatar : "";
        if (!avatarPath.isEmpty()) {
            String full = avatarPath.startsWith("http") ? avatarPath : Constants.IMAGE_BASE_URL + avatarPath;
            Glide.with(context).load(full).placeholder(R.drawable.avatar_default).error(R.drawable.avatar_default).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.avatar_default);
        }

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> f.selected = isChecked;
        holder.cbSelect.setOnCheckedChangeListener(listener);
        convertView.setOnClickListener(v -> {
            f.selected = !f.selected;
            holder.cbSelect.setChecked(f.selected);
        });
        return convertView;
    }

    public ArrayList<Integer> getSelectedIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (FriendChoice f : friends) {
            if (f.selected) ids.add(f.id);
        }
        return ids;
    }

    static class ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvName;
        CheckBox cbSelect;
    }
}
