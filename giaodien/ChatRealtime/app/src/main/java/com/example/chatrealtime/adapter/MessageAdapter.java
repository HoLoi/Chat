package com.example.chatrealtime.adapter;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.chatrealtime.model.MessageModerationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList = new ArrayList<>();
    private final Context context;
    private final int currentUserId;
    private int lastMyMessagePosition = -1;

    public MessageAdapter(Context context, List<Message> initialList, int currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        if (initialList != null) this.messageList = initialList;
        recalculateLastMyMessagePosition();
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
        String mediaUrl = normalizeMediaUrl(fileUrl);
        boolean isMine = msg.isMine();
        MessageModerationStatus moderationStatus = msg.getModerationStatus() != null ? msg.getModerationStatus() : MessageModerationStatus.CLEAN;

        holder.mediaContainer.setVisibility(View.GONE);
        holder.txtMessage.setVisibility(View.VISIBLE);

        int warningColor = ContextCompat.getColor(context, R.color.warning_soft_yellow);
        boolean isWarning = moderationStatus == MessageModerationStatus.WARNING;
        if (holder.messageBubble != null) {
            if (isWarning) {
                holder.messageBubble.setBackgroundColor(warningColor);
            } else {
                holder.messageBubble.setBackgroundResource(isMine ? R.drawable.bg_message_sent : R.drawable.bg_message_received);
            }
        }

        int normalMessageTextColor = isMine ? Color.WHITE : Color.parseColor("#222222");
        int normalTimestampColor = isMine ? Color.parseColor("#E3E7ED") : Color.parseColor("#9AA0A6");
        int warningMetaColor = Color.parseColor("#6B7280");

        holder.txtMessage.setTextColor(isWarning ? Color.parseColor("#222222") : normalMessageTextColor);
        holder.txtTimestamp.setTextColor(isWarning ? warningMetaColor : normalTimestampColor);
        if (holder.txtStatus != null) {
            holder.txtStatus.setTextColor(isWarning ? warningMetaColor : normalTimestampColor);
        }

        String displayText = msg.getNoiDung() != null ? msg.getNoiDung() : "";

        bindAvatar(holder, msg);

        if (type.startsWith("image") && !mediaUrl.isEmpty()) {
            holder.mediaContainer.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(displayText.isEmpty() ? View.GONE : View.VISIBLE);

            Glide.with(context)
                    .load(mediaUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgMedia);
            if (displayText == null || displayText.isEmpty()) {
                displayText = "";
            }
            setupMediaClicks(holder, mediaUrl, "image/*");
        } else if (type.startsWith("video") && !mediaUrl.isEmpty()) {
            holder.mediaContainer.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(displayText.isEmpty() ? View.GONE : View.VISIBLE);

            Glide.with(context)
                    .load(mediaUrl)
                    .thumbnail(0.2f)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgMedia);
            setupMediaClicks(holder, mediaUrl, "video/*");
        }

        holder.txtMessage.setText(displayText);

        boolean showTimestamp = isLastInSenderGroup(position);
        if (showTimestamp) {
            holder.txtTimestamp.setVisibility(View.VISIBLE);
            holder.txtTimestamp.setText(formatTime(msg.getThoiGianGui()));
        } else {
            holder.txtTimestamp.setVisibility(View.GONE);
            holder.txtTimestamp.setText("");
        }

        if (holder.txtStatus != null) {
            boolean showStatus = msg.isMine() && position == lastMyMessagePosition;
            if (showStatus) {
                holder.txtStatus.setVisibility(View.VISIBLE);
                holder.txtStatus.setText(mapStatusLabel(msg.getTrangThaiTinNhan()));
            } else {
                holder.txtStatus.setVisibility(View.GONE);
                holder.txtStatus.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message msg) {
        messageList.add(msg);
        notifyItemInserted(messageList.size() - 1);
        recalculateLastMyMessagePosition();
        if (messageList.size() >= 2) {
            notifyItemChanged(messageList.size() - 2);
        }
    }

    public void addMessages(List<Message> msgs) {
        int start = messageList.size();
        messageList.addAll(msgs);
        notifyItemRangeInserted(start, msgs.size());
        recalculateLastMyMessagePosition();
    }

    public void setMessages(List<Message> msgs) {
        messageList.clear();
        if (msgs != null) {
            messageList.addAll(msgs);
        }
        recalculateLastMyMessagePosition();
        notifyDataSetChanged();
    }

    public Message getLastMessage() {
        if (messageList == null || messageList.isEmpty()) {
            return null;
        }
        return messageList.get(messageList.size() - 1);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageView imgMedia;
        FrameLayout mediaContainer;
        LinearLayout messageBubble;
        TextView avatarView;
        TextView txtTimestamp;
        TextView txtStatus;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgMedia = itemView.findViewById(R.id.imgMedia);
            mediaContainer = itemView.findViewById(R.id.mediaContainer);
            messageBubble = itemView.findViewById(R.id.messageBubble);
            avatarView = itemView.findViewById(R.id.avatarView);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    private boolean isLastInSenderGroup(int position) {
        if (position < 0 || position >= messageList.size()) {
            return false;
        }
        if (position == messageList.size() - 1) {
            return true;
        }
        Message current = messageList.get(position);
        Message next = messageList.get(position + 1);
        return current.getMaNguoiGui() != next.getMaNguoiGui();
    }

    private void recalculateLastMyMessagePosition() {
        lastMyMessagePosition = -1;
        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message message = messageList.get(i);
            if (message.isMine() || message.getMaNguoiGui() == currentUserId) {
                lastMyMessagePosition = i;
                break;
            }
        }
    }

    private String formatTime(String rawTime) {
        if (rawTime == null || rawTime.trim().isEmpty()) {
            return "";
        }
        String value = rawTime.trim();
        try {
            // yyyy-MM-ddTHH:mm:ss -> dd/MM HH:mm
            if (value.contains("T") && value.length() >= 16) {
                String day = value.substring(8, 10);
                String month = value.substring(5, 7);
                String hourMin = value.substring(11, 16);
                return day + "/" + month + " " + hourMin;
            }

            // yyyy-MM-dd HH:mm:ss -> dd/MM HH:mm
            if (value.length() >= 16 && value.charAt(4) == '-' && value.charAt(7) == '-') {
                String day = value.substring(8, 10);
                String month = value.substring(5, 7);
                String hourMin = value.substring(11, 16);
                return day + "/" + month + " " + hourMin;
            }

            // dd/MM HH:mm -> giữ nguyên
            if (value.length() >= 11 && value.charAt(2) == '/' && value.charAt(5) == ' ') {
                return value;
            }

            // HH:mm -> thêm ngày/tháng hiện tại
            if (value.length() == 5 && value.charAt(2) == ':') {
                java.text.SimpleDateFormat dayMonthFmt = new java.text.SimpleDateFormat("dd/MM", Locale.getDefault());
                String dayMonth = dayMonthFmt.format(new java.util.Date());
                return dayMonth + " " + value;
            }
        } catch (Exception ignored) {
            // fallback below
        }
        return value;
    }

    private String mapStatusLabel(String status) {
        String normalized = status != null ? status.trim().toLowerCase(Locale.ROOT) : "sent";
        if ("read".equals(normalized) || "seen".equals(normalized)) {
            return "Đã xem";
        }
        return "Đã gửi";
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

        String fullAvatar = normalizeMediaUrl(avatarUrl);
        if (!fullAvatar.isEmpty()) {
            Glide.with(context)
                    .asDrawable()
                .load(fullAvatar)
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

    private String normalizeMediaUrl(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "/null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("http")) return trimmed;
        if (trimmed.startsWith("/")) return Constants.IMAGE_BASE_URL + trimmed;
        return Constants.IMAGE_BASE_URL + "/" + trimmed;
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
        String[] items = new String[]{"Xem", "Tải về"};
        new AlertDialog.Builder(context)
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openMedia(url, mimeHint);
                            break;
                        case 1:
                            download(url);
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
}
