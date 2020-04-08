package com.example.realtimechat;

public class Message {
    public String name;
    public String content;
    public boolean isMe;

    public Message(String name, String content, boolean isMe) {
        this.name = name;
        this.content = content;
        this.isMe = isMe;
    }
}
