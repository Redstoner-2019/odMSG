package me.redstoner2019.odmsg.misc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static me.redstoner2019.odmsg.server.ServerConnector.mainFile;

public class User {
    private String uuid;
    private String username;
    private String displayname;
    private List<String> friends;
    private List<String> chats;
    private List<String> directMessages;

    public static User nullUser(){
        return new User("","");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User(String username, String displayname){
        this.uuid = UUID.randomUUID().toString();
        this.username = username;
        this.displayname = displayname;
        this.friends = new ArrayList<>();
        this.chats = new ArrayList<>();
        this.directMessages = new ArrayList<>();
    }
    public User(String username){
        this(username,username);
    }

    public User(JSONObject object){
        this.uuid = object.getString("uuid");
        this.username = object.getString("username");
        this.displayname = object.getString("displayname");
        this.friends = new ArrayList<>();
        this.chats = new ArrayList<>();
        this.directMessages = new ArrayList<>();
        for (Object o : object.getJSONArray("friends").toList()) this.friends.add(o.toString());
        for (Object o : object.getJSONArray("chats").toList()) this.chats.add(o.toString());
        for (Object o : object.getJSONArray("direct-messages").toList()) this.directMessages.add(o.toString());
    }

    public void save(){
        File userfile = new File(mainFile + "/users/" + username + "/userinfo.json");
        JSONObject user = new JSONObject();
        user.put("username",username);
        user.put("displayname",displayname);
        user.put("chats",new JSONArray(chats));
        user.put("direct-messages",new JSONArray(directMessages));
        user.put("friends",new JSONArray(friends));
        user.put("uuid", uuid);
        Util.writeJsonToFile(user,userfile);
    }

    public void addDirectMessage(String uuid){
        directMessages.add(uuid);
    }
    public void addChat(String uuid){
        chats.add(uuid);
    }
    public void addFriend(String username){
        friends.add(username);
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayname() {
        return displayname;
    }

    public List<String> getFriends() {
        return friends;
    }

    public List<String> getChats() {
        return chats;
    }

    public List<String> getDirectMessages() {
        return directMessages;
    }

    public User load(){
        File userfile = new File(mainFile + "/users/" + username + "/userinfo.json");
        JSONObject object = new JSONObject(Util.readFile(userfile));
        this.uuid = object.getString("uuid");
        this.username = object.getString("username");
        this.displayname = object.getString("displayname");
        this.friends = new ArrayList<>();
        this.chats = new ArrayList<>();
        this.directMessages = new ArrayList<>();
        for (Object o : object.getJSONArray("friends").toList()) this.friends.add(o.toString());
        for (Object o : object.getJSONArray("chats").toList()) this.chats.add(o.toString());
        for (Object o : object.getJSONArray("direct-messages").toList()) this.directMessages.add(o.toString());
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", displayname='" + displayname + '\'' +
                ", friends=" + friends +
                ", chats=" + chats +
                ", directMessages=" + directMessages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(displayname, user.displayname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, displayname);
    }
}
