package com.example.chatrealtime.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatrealtime.R;
import com.example.chatrealtime.model.Message;

import java.util.ArrayList;
import java.util.List;

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
        holder.txtMessage.setText(msg.getNoiDung());
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
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
        }
    }
}
