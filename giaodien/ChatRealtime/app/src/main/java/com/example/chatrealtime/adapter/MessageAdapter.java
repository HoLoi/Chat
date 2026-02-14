package com.example.chatrealtime.adapter;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chatrealtime.R;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList = new ArrayList<>();
    private Context context;

    public MessageAdapter(Context context, List<Message> initialList) {
        this.context = context;
        if (initialList != null) this.messageList = initialList;
    }

    @Override
    public int getItemViewType(int position) {
        // Tin nhắn của chính mình hiển thị bên phải, người khác bên trái
        return messageList.get(position).isMine() ? 1 : 0;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        String type = msg.getLoaiTinNhan() != null ? msg.getLoaiTinNhan() : "text";
        String fileUrl = msg.getDuongDanFile();
        boolean isMine = msg.isMine();

        holder.mediaContainer.setVisibility(View.GONE);
        holder.txtMessage.setVisibility(View.VISIBLE);

        String displayText = msg.getNoiDung() != null ? msg.getNoiDung() : "";

        bindAvatar(holder, msg);

        if (type.startsWith("image") && fileUrl != null && !fileUrl.isEmpty()) {
            holder.mediaContainer.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(displayText.isEmpty() ? View.GONE : View.VISIBLE);

            String fullUrl = fileUrl.startsWith("http") ? fileUrl : (Constants.IMAGE_BASE_URL + fileUrl);
            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgMedia);
            if (displayText == null || displayText.isEmpty()) {
                displayText = "";
            }
            setupMediaClicks(holder, fullUrl, "image/*");
        } else if (type.startsWith("video") && fileUrl != null && !fileUrl.isEmpty()) {
            holder.mediaContainer.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(displayText.isEmpty() ? View.GONE : View.VISIBLE);

            String fullUrl = fileUrl.startsWith("http") ? fileUrl : (Constants.IMAGE_BASE_URL + fileUrl);
            Glide.with(context)
                    .load(fullUrl)
                    .thumbnail(0.2f)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgMedia);
            setupMediaClicks(holder, fullUrl, "video/*");
        }

        holder.txtMessage.setText(displayText);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message msg) {
        messageList.add(msg);
        notifyItemInserted(messageList.size() - 1);
    }

    public void addMessages(List<Message> msgs) {
        int start = messageList.size();
        messageList.addAll(msgs);
        notifyItemRangeInserted(start, msgs.size());
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageView imgMedia;
        FrameLayout mediaContainer;
        TextView avatarView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgMedia = itemView.findViewById(R.id.imgMedia);
            mediaContainer = itemView.findViewById(R.id.mediaContainer);
            avatarView = itemView.findViewById(R.id.avatarView);
        }
    }

    private void bindAvatar(MessageViewHolder holder, Message msg) {
        String avatarUrl = msg.getAnhDaiDien();
        String name = msg.getTenNguoiGui();
        String fallbackText = "";

        if (name != null && !name.isEmpty()) {
            fallbackText = name.substring(0, 1).toUpperCase(Locale.getDefault());
        } else {
            String senderIdStr = String.valueOf(msg.getMaNguoiGui());
            fallbackText = senderIdStr.substring(senderIdStr.length() - Math.min(2, senderIdStr.length())).toUpperCase(Locale.getDefault());
        }

        Drawable placeholder = ContextCompat.getDrawable(context, R.drawable.avatar_circle);
        final String initials = fallbackText;

        if (avatarUrl != null && !avatarUrl.isEmpty() && !"null".equalsIgnoreCase(avatarUrl)) {
            String full = avatarUrl.startsWith("http") ? avatarUrl : (Constants.IMAGE_BASE_URL + avatarUrl);
            Glide.with(context)
                    .asDrawable()
                    .load(full)
                    .circleCrop()
                    .placeholder(placeholder)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.avatarView.setBackground(resource);
                            holder.avatarView.setText("");
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholderDrawable) {
                            holder.avatarView.setBackground(placeholderDrawable);
                            holder.avatarView.setText(initials);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            holder.avatarView.setBackground(errorDrawable != null ? errorDrawable : placeholder);
                            holder.avatarView.setText(initials);
                        }
                    });
        } else {
            holder.avatarView.setText(initials);
            holder.avatarView.setText(fallbackText);
        }
    }

    private void setupMediaClicks(MessageViewHolder holder, String fullUrl, String mimeHint) {
        holder.mediaContainer.setOnClickListener(v -> openMedia(fullUrl, mimeHint));
        holder.mediaContainer.setOnLongClickListener(v -> {
            showMediaActions(fullUrl, mimeHint);
            return true;
        });
    }

    private void openMedia(String url, String mimeHint) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), mimeHint);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // fallback: open generic
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void showMediaActions(String url, String mimeHint) {
        String[] items = new String[]{"Xem", "Tải về", "Sao chép link"};
        new AlertDialog.Builder(context)
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openMedia(url, mimeHint);
                            break;
                        case 1:
                            download(url);
                            break;
                        case 2:
                            copyLink(url);
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    private void download(String url) {
        try {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) return;
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Uri.parse(url).getLastPathSegment());
            dm.enqueue(req);
        } catch (Exception ignored) {}
    }

    private void copyLink(String url) {
        try {
            android.content.ClipboardManager cm = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(android.content.ClipData.newPlainText("media", url));
            }
        } catch (Exception ignored) {}
    }
}
