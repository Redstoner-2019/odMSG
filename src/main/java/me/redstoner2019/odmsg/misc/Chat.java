package me.redstoner2019.odmsg.misc;

import me.redstoner2019.odmsg.server.ServerConnector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Chat {
    private List<String> participants = new ArrayList<>();
    private final String uuid;
    private String title;
    private int segments;
    private long messages;
    private long latestMessage = 0;
    private JSONObject segmentData;
    private HashMap<Integer, JSONObject> chatsegments = new HashMap<>();
    public static int MAX_SEGMENT_LENGTH = 10;
    private File chatInfoFile = new File(ServerConnector.mainFile + "/chats/" + getUuid() + "/chatinfo.json");
    private Logger logger;

    public long getMessages() {
        return messages;
    }

    public static Chat createNewChat(String title){
        Chat chat = new Chat();
        chat.addNewSegment();
        chat.setTitle(title);
        chat.addMessage(new Message("Chat initialized.","SERVER"));
        chat.save();
        return chat;
    }

    public int getSegments() {
        return segments;
    }

    public void setSegments(int segments) {
        this.segments = segments;
    }

    public Chat() {
        participants = new ArrayList<>();
        uuid = UUID.randomUUID().toString();
        segments = 0;
        segmentData = new JSONObject();
        save();
        logger = new Logger(uuid);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Chat(String uuid){
        this.uuid = uuid;
        chatInfoFile = new File(ServerConnector.mainFile + "/chats/" + getUuid() + "/chatinfo.json");
        /**
         * Loading
         */
        JSONObject object = null;
        object = new JSONObject(Util.readFile(chatInfoFile));

        JSONArray participantsArray = object.getJSONArray("participants");
        for (int i = 0; i < participantsArray.length(); i++) {
            participants.add(participantsArray.getString(i));
        }
        this.segments = object.getInt("segments");
        this.messages = object.getLong("messages");
        this.latestMessage = object.getLong("last-msg");
        this.segmentData = object.getJSONObject("segment-data");
        this.title = object.getString("title");
        logger = new Logger(uuid);
    }

    public void addParticipant(String username){
        if(participants.contains(username)) return;
        participants.add(username);
    }

    public void removeParticipant(String username){
        if(!participants.contains(username)) return;
        participants.remove(username);
    }

    public int segmentForMessageIndex(long msgID){
        if(msgID > messages) msgID = messages;
        for(int i = 1; i < segments+1; i++){
            long min = segmentData.getJSONObject(i + "").getLong("min");
            long max = segmentData.getJSONObject(i + "").getLong("max");
            if(min <= msgID && max >= msgID) return i;
        }
        return -1;
    }

    public void addMessage(Message message){
        try{
            JSONObject segment = getSegment(getSegments());
            JSONArray messages = segment.getJSONArray("messages");
            if(messages.length() >= MAX_SEGMENT_LENGTH){
                addNewSegment();
                segment = getSegment(getSegments());
                messages = segment.getJSONArray("messages");
            }

            JSONObject msgObj = new JSONObject();
            msgObj.put("message",message.toJson());
            msgObj.put("message-id",this.messages);

            messages.put(msgObj);
            segment.put("messages",messages);
            chatsegments.put(getSegments(),segment);

            JSONObject segObject = segmentData.getJSONObject(getSegments() + "");
            segObject.put("max",this.messages);
            segmentData.put(getSegments() + "",segObject);

            this.messages++;

            latestMessage = System.currentTimeMillis();

            save();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public long getLatestMessageTime() {
        return latestMessage;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isParticipant(String username){
        return participants.contains(username);
    }
    public JSONObject toJson(){
        JSONObject object = new JSONObject();
        object.put("uuid",uuid);
        object.put("messages",messages);
        object.put("segments",segments);
        object.put("segment-data",segmentData);
        object.put("title",title);
        object.put("last-msg",latestMessage);
        JSONArray participantsArray = new JSONArray();
        for(String participant : participants){
            participantsArray.put(participant);
        }
        object.put("participants",participantsArray);
        return object;
    }
    public JSONObject getSegment(int id){
        JSONObject data = chatsegments.getOrDefault(id,null);
        if(data == null){
            File file = new File(ServerConnector.mainFile + "/chats/" + uuid + "/chats/" + id + ".json");
            if(file.exists()){
                return new JSONObject(Util.readFile(file));
            } else {
                logger.err("File " + file + " not found");
            }
        }
        return data;
    }
    public void addNewSegment(){
        setSegments(getSegments() + 1);
        JSONObject newsegment = new JSONObject();
        newsegment.put("messages",new JSONArray());
        chatsegments.put(segments,newsegment);

        JSONObject segData = new JSONObject();
        segData.put("min",this.messages);
        segData.put("max",this.messages);

        segmentData.put(getSegments() + "", segData);
    }
    public void save(){
        try {
            JSONObject chatInfo = toJson();
            File file = new File(ServerConnector.mainFile + "/chats/" + uuid + "/chatinfo.json");
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Util.writeJsonToFile(chatInfo,file);
            for (int i : chatsegments.keySet()){
                file = new File(ServerConnector.mainFile + "/chats/" + uuid + "/chats/" + i + ".json");
                if(!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                Util.writeJsonToFile(chatsegments.get(i),file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
