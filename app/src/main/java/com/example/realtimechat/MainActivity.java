package com.example.realtimechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    EditText edtName;
    EditText edtRoomID;
    Button btnEnter;

    private Boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnected()) {
            return true;
        }
        return false;
    }

    final String URL_SERVER = "https://chatbtt.herokuapp.com/";
    Socket socket;
    {
        try {
            socket = IO.socket(URL_SERVER);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket.connect();

        edtName = findViewById(R.id.edtName);
        edtRoomID = findViewById(R.id.edtRoom);
        btnEnter = findViewById(R.id.btnEnter);

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false) {
            wifi.setWifiEnabled(true);
        }

        boolean checkOnline = isOnline();
        if (!checkOnline){
            Toast.makeText(MainActivity.this, "Requires network!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Connect network success", Toast.LENGTH_SHORT).show();
        }

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().length() <= 0 || edtRoomID.getText().length() <= 0){
                    Toast.makeText(MainActivity.this, "Input full name and room ID", Toast.LENGTH_LONG).show();
                } else {

                    socket.emit("client-send-roomId", edtRoomID.getText());
                    Intent intent = new Intent(MainActivity.this, Chat.class);
                    intent.putExtra("fullName", edtName.getText().toString());
                    intent.putExtra("roomID", edtRoomID.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }

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
