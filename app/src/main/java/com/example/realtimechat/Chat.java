package com.example.realtimechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {

    TextView txtFullName;
    TextView txtRoomID;
    EditText edtMessage;
    ImageView btnSend;
    ListView listView;

    final String URL_SERVER = "https://chatbtt.herokuapp.com/";
    Socket socket;
    {
        try {
            socket = IO.socket(URL_SERVER);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    List<Message> list = new ArrayList<>();
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        socket.connect();

        txtFullName = findViewById(R.id.txtFullName);
        txtRoomID = findViewById(R.id.txtRoomID);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        listView = findViewById(R.id.msgShow);

        Intent intent = getIntent();
        String fullName = intent.getStringExtra("fullName");
        String roomID = intent.getStringExtra("roomID");

        txtFullName.setText(fullName);
        txtRoomID.setText(roomID);

        chatAdapter = new ChatAdapter(Chat.this, list);
        listView.setAdapter(chatAdapter);

        socket.on("server-send-message", onNewMessage);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtMessage.getText().length() <= 0){
                    Toast.makeText(Chat.this, "Input a message", Toast.LENGTH_LONG).show();
                } else {
                    socket.emit("client-send-message", (txtFullName.getText() + ": " + edtMessage.getText()));
                    list.add(new Message(null, edtMessage.getText().toString(), true));
                    chatAdapter.notifyDataSetChanged();
                    listView.setSelection(list.size() - 1);
                    edtMessage.setText("");
                }
            }
        });
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String name = args[0].toString().split(": ")[0];
                    String content = args[0].toString().split(": ")[1];
                    Message message = new Message(name, content, false);
                    list.add(message);
                    chatAdapter.notifyDataSetChanged();
                    listView.setSelection(list.size() - 1);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}