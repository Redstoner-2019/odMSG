package me.redstoner2019.odmsg.server;

import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.events.ClientConnectEvent;
import me.redstoner2019.events.ConnectionLostEvent;
import me.redstoner2019.events.ObjectRecieveEvent;
import me.redstoner2019.odmsg.misc.*;
import me.redstoner2019.server.ODClientHandler;
import me.redstoner2019.server.ODServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerConnector extends ODServer {
    private AuthenticatorClient authenticatorClient = new AuthenticatorClient("localhost",8009);
    public static String mainFile = "odMSG/data";
    public static HashMap<String, Chat> loadedChats = new HashMap<>();
    public static long messageID = 0;

    public ServerConnector() {
        File baseFile = new File(mainFile);
        if(!baseFile.exists()){
            baseFile.mkdirs();
        }
        baseFile = new File(mainFile + "/users");
        if(!baseFile.exists()){
            baseFile.mkdirs();
        }
        baseFile = new File(mainFile + "/files");
        if(!baseFile.exists()){
            baseFile.mkdirs();
        }
        baseFile = new File(mainFile + "/chats");
        if(!baseFile.exists()){
            baseFile.mkdirs();
        }
        baseFile = new File(mainFile + "/server.json");
        if(!baseFile.exists()){
            try {
                baseFile.createNewFile();
                JSONObject baseData = new JSONObject();
                baseData.put("messageID",messageID);
                Util.writeJsonToFile(baseData,baseFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        JSONObject baseData = new JSONObject(Util.readFile(baseFile));
        messageID = baseData.getLong("messageID");

        /*User user = createAccount("lukas","Lukas");
        Chat chat1 = Chat.createNewChat("Test Chat");
        chat1.addMessage(new Message("test","test1"));
        user.addDirectMessage(chat1.getUuid());
        chat1.addParticipant(user.getUsername());
        user.save();
        chat1.save();*/

        System.out.println(getUser("lukas"));

        authenticatorClient.setup();
        if(!authenticatorClient.isConnected()){
            throw new RuntimeException("Authenticator not connected");
        }
        setPort(8005);

        addClientConnectEvent(new ClientConnectEvent() {
            @Override
            public void onEvent(ODClientHandler odClientHandler) {
                User user = User.nullUser();
                System.out.println("Client connected");
                odClientHandler.startListener();
                odClientHandler.addObjectRecieveEvent(new ObjectRecieveEvent() {
                    @Override
                    public void onEvent(Object o) {
                        System.out.println(o.getClass());
                        if(o instanceof String){
                            JSONObject packet = new JSONObject((String) o);
                            System.out.println(packet.toString());
                            if(packet.has("header")){
                                switch (packet.getString("header")){
                                    case "login": {
                                        JSONObject info = authenticatorClient.tokeninfo(packet.getString("token"));

                                        JSONObject resultData = new JSONObject();
                                        resultData.put("header","login-result");
                                        resultData.put("result",info.getString("data"));

                                        createAccount(info.getString("username"),info.getString("displayname"));

                                        user.setUsername(info.getString("username"));

                                        user.load();

                                        odClientHandler.sendObject(resultData.toString());
                                        break;
                                    }
                                    case "data-request" : {
                                        if(user.equals(User.nullUser())) {
                                            break;
                                        } else {
                                            user.load();
                                        }

                                        JSONObject resultData = new JSONObject();
                                        resultData.put("header","data-result");

                                        user.load();

                                        resultData.put("direct-messages", user.getDirectMessages());
                                        resultData.put("chats", user.getChats());
                                        resultData.put("friends", user.getFriends());

                                        odClientHandler.sendObject(resultData.toString());
                                        break;
                                    }
                                    case "send-message" : {
                                        try{
                                            loadChat(packet.getString("chat"));
                                            Chat chat = loadedChats.get(packet.getString("chat"));
                                            if(chat.isParticipant(user.getUsername())) {
                                                Message message = new Message(packet.getJSONObject("data"));
                                                message.setId(messageID++);
                                                chat.addMessage(message);
                                            }
                                            save();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
                                    case "request-messages" : {
                                        try{
                                            String uuid = packet.getString("chat");
                                            long start = packet.getLong("message-start");
                                            long end = packet.getLong("message-end");
                                            loadChat(uuid);
                                            Chat chat = loadedChats.get(uuid);
                                            if(chat.isParticipant(user.getUsername())) {
                                                JSONArray messages = new JSONArray();
                                                JSONArray msges = chat.getSegment(chat.getSegments()).getJSONArray("messages");

                                                int startSegment = chat.segmentForMessageIndex(start);
                                                int endSegment = chat.segmentForMessageIndex(end);

                                                for (int i = startSegment; i <= endSegment; i++) {
                                                    msges = chat.getSegment(i).getJSONArray("messages");
                                                    for (int j = 0; j < msges.length(); j++) {
                                                        JSONObject jsonMessageData = msges.getJSONObject(j);
                                                        long messageID = jsonMessageData.getLong("message-id");
                                                        if(messageID < start || messageID > end){
                                                            continue;
                                                        }
                                                        messages.put(jsonMessageData);
                                                    }
                                                }
                                                JSONObject response = new JSONObject();
                                                response.put("header","request-messages");
                                                response.put("data",messages);
                                                odClientHandler.sendObject(response.toString());
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                        break;
                                    }
                                    case "msg-update-check" : {
                                        String uuid = packet.getString("chat");

                                        loadChat(uuid);
                                        Chat chat = loadedChats.get(uuid);

                                        JSONObject object = new JSONObject();
                                        object.put("header","msg-update-check");
                                        object.put("current-msg-id",chat.getMessages());
                                        odClientHandler.sendObject(object.toString());
                                        break;
                                    }
                                    case "chat-info" : {
                                        String uuid = packet.getString("chat");

                                        loadChat(uuid);
                                        Chat chat = loadedChats.get(uuid);
                                        JSONObject object = new JSONObject();
                                        object.put("header","chat-info");
                                        object.put("title",chat.getTitle());
                                        object.put("chat",uuid);
                                        object.put("last-msg",chat.getLatestMessageTime());
                                        object.put("members",new JSONArray(chat.getParticipants()));
                                        odClientHandler.sendObject(object.toString());
                                        break;
                                    }
                                    case "file-upload" : {
                                        JSONObject dataObject = packet.getJSONObject("data");
                                        System.out.println("Upload recieved");
                                        System.out.println(dataObject.toString());
                                        String savePath;
                                        switch (dataObject.getString(dataObject.getString("type"))) {
                                            case "profile-picture" : {
                                                savePath = "odMSG/data/users/" + user.getUsername() + "/profile_picture.png";
                                                break;
                                            }
                                            default: {
                                                savePath = "error.txt";
                                                break;
                                            }
                                        }
                                        FileTransfer.downloadFile(savePath,odClientHandler.getSocket());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });

                odClientHandler.addConnectionLostEvent(new ConnectionLostEvent() {
                    @Override
                    public void onEvent() {
                        System.out.println("Connection Lost");
                    }
                });
            }
        });

        start();
    }

    private User createAccount(String username, String displayname){
        if(accountExists(username)) return new User(username,displayname).load();

        User user = new User(username,displayname);

        File userfile = new File(mainFile + "/users/" + username);
        userfile.mkdirs();
        userfile = new File(mainFile + "/users/" + username + "/profile_picture.png");
        try {
            userfile.createNewFile();
            userfile = new File(mainFile + "/users/" + username + "/userinfo.json");
            userfile.createNewFile();
            user.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return getUser(username);
    }
    private boolean accountExists(String username){
        return new File(mainFile + "/users/" + username + "/userinfo.json").exists();
    }
    private User getUser(String username){
        File userfile = new File(mainFile + "/users/" + username + "/userinfo.json");
        return new User(new JSONObject(Util.readFile(userfile)));
    }
    private void loadChat(String uuid){
        if(loadedChats.containsKey(uuid)) return;
        Chat chat = new Chat(uuid);
        loadedChats.put(uuid,chat);
    }
    private void save(){
        JSONObject object = new JSONObject();
        File baseFile = new File(mainFile + "/server.json");
        object.put("messageID",messageID);
        Util.writeJsonToFile(object,baseFile);
    }
}
