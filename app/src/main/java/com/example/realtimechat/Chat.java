package com.example.realtimechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.example.realtimechat.App.CHANNEL_1_ID;
import static com.example.realtimechat.App.CHANNEL_2_ID;

public class Chat extends AppCompatActivity {

    final String URL_SERVER = Config.URL_SERVER_CHAT;

    private NotificationManagerCompat notificationManager;

    TextView txtFullName;
    TextView txtRoomID;
    EditText edtMessage;
    ImageView btnSend;
    ListView listView;
    TextView txtNumOfRoom;

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

        notificationManager = NotificationManagerCompat.from(this);

        txtFullName = findViewById(R.id.txtFullName);
        txtRoomID = findViewById(R.id.txtRoomID);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        listView = findViewById(R.id.msgShow);
        txtNumOfRoom = findViewById(R.id.txtNumOfRoom);

        Intent intent = getIntent();
        String fullName = intent.getStringExtra("fullName");
        String roomID = intent.getStringExtra("roomID");

        txtFullName.setText(fullName);
        txtRoomID.setText(roomID);

        chatAdapter = new ChatAdapter(Chat.this, list);
        listView.setAdapter(chatAdapter);

        socket.on("server-send-message", onNewMessage);
        socket.on("socket-send-room", onNumOfRoom);
        socket.on("server-send-num-in-room", onUpdateNumOfRoom);
        socket.on("socket-typing", onSbIsTyping);
        socket.on("socket-stop-typing", onSbStopTyping);

        edtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    socket.emit("typing");
                } else {
                    socket.emit("stop-typing");
                }
            }
        });

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

    public void sendOnChannel1(String name, String content) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.android_chat);

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.android_chat)
                    .setLargeIcon(bitmap)
                    .setContentTitle(name)
                    .setContentText(content)
                    .setAutoCancel(true)
                     .build();

        r.play();
        notificationManager.notify(1, notification);
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
                    sendOnChannel1(name, content);
                }
            });
        }
    };

    private Emitter.Listener onNumOfRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    String numOfOnline;
                    try {
                        numOfOnline = object.getString("numOfOnline");
                        txtNumOfRoom.setText(numOfOnline);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onUpdateNumOfRoom = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = args[0].toString();
                    txtNumOfRoom.setText(data);
                }
            });
        }
    };

    private Emitter.Listener onSbIsTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    edtMessage.setHint("Someone is typing...");
                }
            });
        }
    };

    private Emitter.Listener onSbStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    edtMessage.setHint("Write a message...");
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
