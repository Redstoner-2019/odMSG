package me.redstoner2019.odmsg.misc;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Message {
    private String content;
    private String senderUsername;
    private long sendTime;
    private long id = -1;
    private String encryption = "NONE";

    public Message(String content, String senderUsername) {
        this.content = content;
        this.senderUsername = senderUsername;
        this.sendTime = System.currentTimeMillis();
    }
    public Message(JSONObject object) {
        this.content = object.getString("content");
        this.senderUsername = object.getString("sender");
        this.sendTime = object.getLong("send-time");
        this.encryption = object.getString("encryption");
        if(object.has("id")){
            this.id = object.getLong("id");
        }
    }
    public JSONObject toJson(){
        JSONObject object = new JSONObject();
        object.put("content",content);
        object.put("sender",senderUsername);
        object.put("send-time",sendTime);
        object.put("encryption",encryption);
        object.put("id",id);
        return object;
    }

    public String getContent() {
        return content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sendTime);
        return "<html><span style=\"font-size: 16px;\">" + senderUsername + " </span><span style=\"font-size: 8px;\">" + new Date(sendTime).toGMTString() + "</span> <br><br> <span style=\"font-size: 14px;\">" + content + "</span></html>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return sendTime == message.sendTime && id == message.id && Objects.equals(content, message.content) && Objects.equals(senderUsername, message.senderUsername) && Objects.equals(encryption, message.encryption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, senderUsername, sendTime, id, encryption);
    }
}
