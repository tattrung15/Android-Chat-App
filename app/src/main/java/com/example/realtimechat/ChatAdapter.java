package com.example.realtimechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    List<Message> list;

    public ChatAdapter(Context context, List<Message> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Message message = list.get(position);

        if (message.isMe){
            convertView = inflater.inflate(R.layout.message_send, null);
            TextView txtSend = convertView.findViewById(R.id.txtMsg_send);
            txtSend.setText(list.get(position).content);
        } else {
            convertView = inflater.inflate(R.layout.message_received, null);
            TextView txtMsg_Received = convertView.findViewById(R.id.txtMsg_received);
            TextView txtName_received = convertView.findViewById(R.id.txtName_received);
            txtMsg_Received.setText(list.get(position).content);
            txtName_received.setText(list.get(position).name);
        }

        return convertView;
    }
}
