package me.redstoner2019.odmsg.client;

import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.events.ConnectionFailedEvent;
import me.redstoner2019.events.ConnectionLostEvent;
import me.redstoner2019.events.ConnectionSuccessEvent;
import me.redstoner2019.events.ObjectRecieveEvent;
import me.redstoner2019.odmsg.misc.AlternatingListCellRenderer;
import me.redstoner2019.odmsg.misc.Logger;
import me.redstoner2019.odmsg.misc.Message;
import me.redstoner2019.odmsg.misc.VerticalCellRenderer;
import me.redstoner2019.util.Token;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GUI extends JFrame {
    private Container loginGUI = new Container();
    private Container mainGUI = new Container();
    private Container chatGUI = new Container();
    private Container callGUI = new Container();
    private Container manageGUI = new Container();
    private Container friendGUI = new Container();

    private ClientConnector connector = new ClientConnector();
    private AuthenticatorClient authenticatorClient = new AuthenticatorClient();
    private Logger logger = new Logger("CLIENT");

    private JList<String> directMessages = new JList<>();
    private JList<String> chats = new JList<>();
    private JList<String> friends = new JList<>();
    private List<Message> cachedMessages = new ArrayList<>();
    private JList<Message> messages = new JList<>();
    private HashMap<String,String> usernameDisplaynameCache = new HashMap<>();
    private HashMap<String,JSONObject> chatInfoCache = new HashMap<>();
    private List<String> directMessagesUUIDs = new ArrayList<>();
    private List<String> chatsUUIDs = new ArrayList<>();
    private List<String> friendsUsernames = new ArrayList<>();

    private String username;
    private String currentChatUUID = null;

    public GUI(){
        setTitle("odMSG");
        setSize(1280,720);
        setLayout(null);
        setLocationRelativeTo(null);

        initLoginGUI();
        initMainGUI();
        initChatGUI();
        initCallGUI();
        initManageGUI();
        initFriendGUI();

        switchContentPane(loginGUI);

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        authenticatorClient.setup();
    }
    private void initLoginGUI(){
        GUI instance = this;
        JLabel lblTitle = new JLabel("odMSG");
        JTextField txtUsername = new JTextField("lukas");
        JTextField txtPassword = new JTextField("test");
        JLabel lblUsername = new JLabel("Username");
        JLabel lblPassword = new JLabel("Password");
        JButton loginBtn = new JButton("Login");
        JButton createBtn = new JButton("Create");
        JLabel lblConnectStatus = new JLabel("");

        lblTitle.setFont(new Font("Arial",Font.PLAIN,25));
        lblTitle.setHorizontalAlignment(JLabel.CENTER);

        lblTitle.setBounds(0,0,1280,30);
        txtUsername.setBounds(1280/2 + 20,80,200,30);
        txtPassword.setBounds(1280/2 + 20,110,200,30);
        lblUsername.setBounds(1280/2 - 220,80,200,30);
        lblPassword.setBounds(1280/2 - 220,110,200,30);
        loginBtn.setBounds(1280/2 - 220,160,200,30);
        createBtn.setBounds(1280/2 + 20,160,200,30);
        lblConnectStatus.setBounds(1280/2 - 220,190,440,40);

        loginGUI.add(lblTitle);
        loginGUI.add(txtUsername);
        loginGUI.add(txtPassword);
        loginGUI.add(lblUsername);
        loginGUI.add(lblPassword);
        loginGUI.add(loginBtn);
        loginGUI.add(createBtn);
        loginGUI.add(lblConnectStatus);

        connector.addConnectionFailedEvent(new ConnectionFailedEvent() {
            @Override
            public void onEvent() {
                logger.log("Connection Failed");
                lblConnectStatus.setText("Connection Failed");
            }
        });

        connector.addConnectionLostEvent(new ConnectionLostEvent() {
            @Override
            public void onEvent() {
                logger.log("Connection Lost");
                lblConnectStatus.setText("Connection Lost");
            }
        });

        connector.addConnectionSuccessEvent(new ConnectionSuccessEvent() {
            @Override
            public void onEvent() {
                //JSONObject packet = authenticatorClient.loginAccount(txtUsername.getText(), Token.hashPassword(txtPassword.getText()));
                lblConnectStatus.setText("Requesting Token");
                JSONObject packet = authenticatorClient.loginAccount(txtUsername.getText(), txtPassword.getText());

                if(!packet.getString("data").equals("login-success")){
                    logger.log(packet.getString("data"));
                    lblConnectStatus.setText(packet.getString("data"));
                    connector.disconnect();
                    return;
                }

                connector.startListener();

                JSONObject loginPacket = new JSONObject();

                loginPacket.put("header","login");
                loginPacket.put("token",packet.getString("token"));

                lblConnectStatus.setText("Sending login request...");

                connector.sendObject(loginPacket.toString());

                lblConnectStatus.setText("Logging in...");

                startChatUpdater();
            }
        });

        connector.addObjectReiecedEvent(new ObjectRecieveEvent() {
            @Override
            public void onEvent(Object o) {
                if(o instanceof String){
                    JSONObject packet = new JSONObject((String) o);
                    switch (packet.getString("header")){
                        case "login-result": {
                            switch (packet.getString("result")){
                                case "success" : {
                                    logger.log("login confirmed");
                                    lblConnectStatus.setText("Login confirmed");
                                    switchContentPane(mainGUI);

                                    updateData();

                                    username = txtUsername.getText();

                                    break;
                                }
                            }
                            break;
                        }
                        case "data-result" : {
                            directMessagesUUIDs = new ArrayList<>();
                            chatsUUIDs = new ArrayList<>();
                            friendsUsernames = new ArrayList<>();

                            JSONArray dataArray = packet.getJSONArray("direct-messages");
                            for (int i = 0; i < dataArray.length(); i++) {
                                directMessagesUUIDs.add(dataArray.getString(i));
                            }

                            dataArray = packet.getJSONArray("chats");
                            for (int i = 0; i < dataArray.length(); i++) {
                                chatsUUIDs.add(dataArray.getString(i));
                            }

                            dataArray = packet.getJSONArray("friends");
                            for (int i = 0; i < dataArray.length(); i++) {
                                friendsUsernames.add(dataArray.getString(i));
                            }
                            break;
                        }
                        case "msg-update-check" : {
                            long messagesAvailable = packet.getLong("current-msg-id");
                            if(messagesAvailable == cachedMessages.size()){
                                break;
                            }
                            JSONObject request = new JSONObject();
                            request.put("header","request-messages");
                            request.put("chat",currentChatUUID);
                            long min = Math.max(messagesAvailable-50,1);
                            long max = messagesAvailable;
                            request.put("message-start",min-1);
                            request.put("message-end",max-1);
                            connector.sendObject(request.toString());
                            break;
                        }
                        case "request-messages" : {
                            JSONArray messages = packet.getJSONArray("data");
                            List<JSONObject> messagesList = new ArrayList<>();
                            for (int i = 0; i < messages.length(); i++) {
                                messagesList.add(messages.getJSONObject(i));
                            }
                            messagesList.sort(new Comparator<JSONObject>() {
                                @Override
                                public int compare(JSONObject o1, JSONObject o2) {
                                    return (int) (o1.getLong("message-id") - o2.getLong("message-id"));
                                }
                            });

                            for(JSONObject msg : messagesList){
                                if(!cachedMessages.contains(new Message(msg.getJSONObject("message")))) cachedMessages.add(new Message(msg.getJSONObject("message")));
                            }

                            Message[] data = new Message[cachedMessages.size()];
                            for (int i = 0; i < data.length; i++) {
                                data[data.length - i-1] = cachedMessages.get(i);
                            }
                            instance.messages.setListData(data);
                            break;
                        }
                        case "chat-info" : {
                            JSONObject data = new JSONObject(packet.toString());
                            data.remove("header");
                            chatInfoCache.put(data.getString("chat"),data);
                            break;
                        }
                    }
                }
            }
        });

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connector.connect("localhost",8005);
            }
        });

        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONObject result = authenticatorClient.createAccount(txtUsername.getText(),txtUsername.getText(),txtPassword.getText());
                logger.log(result);
            }
        });
    }

    private void initMainGUI(){
        JLabel lblTitle = new JLabel("odMSG");
        JScrollPane directMessagesScrollPane = new JScrollPane(directMessages);
        JScrollPane chatsScrollPane = new JScrollPane(chats);
        JScrollPane friendsScrollPane = new JScrollPane(friends);

        lblTitle.setFont(new Font("Arial",Font.PLAIN,25));
        directMessagesScrollPane.setFont(new Font("Arial",Font.PLAIN,25));
        chatsScrollPane.setFont(new Font("Arial",Font.PLAIN,25));
        friendsScrollPane.setFont(new Font("Arial",Font.PLAIN,25));
        lblTitle.setHorizontalAlignment(JLabel.CENTER);

        lblTitle.setBounds(0,0,1280,30);
        directMessagesScrollPane.setBounds(50,50,200,400);
        chatsScrollPane.setBounds(300,50,200,400);
        friendsScrollPane.setBounds(550,50,200,400);

        directMessages.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(directMessages.getSelectedIndex() == -1) return;
                currentChatUUID = directMessagesUUIDs.get(directMessages.getSelectedIndex());
                JSONObject request = new JSONObject();
                request.put("header","msg-update-check");
                request.put("chat",currentChatUUID);
                connector.sendObject(request.toString());
                switchContentPane(chatGUI);
            }
        });

        chats.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(chats.getSelectedIndex() == -1) return;
                currentChatUUID = chatsUUIDs.get(chats.getSelectedIndex());
                JSONObject request = new JSONObject();
                request.put("header","msg-update-check");
                request.put("chat",currentChatUUID);
                connector.sendObject(request.toString());
                switchContentPane(chatGUI);
            }
        });

        friends.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JSONObject request = new JSONObject();
                request.put("header","friend-info");
                request.put("friend",friends.getSelectedValue());
                connector.sendObject(request.toString());
                switchContentPane(friendGUI);
            }
        });

        mainGUI.add(lblTitle);
        mainGUI.add(directMessagesScrollPane);
        mainGUI.add(chatsScrollPane);
        mainGUI.add(friendsScrollPane);

        logger.log("init done");
    }

    private JLabel chatTitle = new JLabel();
    private JLabel iconLabel = new JLabel();
    private JList<String> membersJList = new JList<>();

    private void initChatGUI(){
        JScrollPane scrollPane = new JScrollPane(messages);
        JButton sendMessage = new JButton("Send");
        JTextArea messageArea = new JTextArea();
        JScrollPane messageAreaScrollPane = new JScrollPane(messageArea);
        JButton backButton = new JButton("<- Back");
        JSeparator separator = new JSeparator();
        JLabel membersLabel = new JLabel("--- Members ---");
        JScrollPane membersScrollPane = new JScrollPane(membersJList);
        JButton management = new JButton("Management");
        JButton joinCall = new JButton("Join Call");

        scrollPane.setBounds(30,90,870,500);
        messageAreaScrollPane.setBounds(30,610,770,50);
        sendMessage.setBounds(820,610,80,50);
        backButton.setBounds(30,30,80,30);
        chatTitle.setBounds(110,0,920-110,40);
        separator.setBounds(920,20,10,getHeight()-80);
        iconLabel.setBounds(990,30,200,200);
        membersLabel.setBounds(990,260,200,20);
        membersScrollPane.setBounds(940,290,300,300);
        joinCall.setBounds(940,610,140,50);
        management.setBounds(940+140+20,610,140,50);

        chatTitle.setFont(new Font("Arial", Font.PLAIN,30));
        chatTitle.setHorizontalAlignment(JLabel.CENTER);
        separator.setOrientation(SwingConstants.VERTICAL);
        iconLabel.setIcon(new ImageIcon(new BufferedImage(iconLabel.getWidth(),iconLabel.getHeight(),1)));
        membersLabel.setHorizontalAlignment(JLabel.CENTER);
        messages.setCellRenderer(new AlternatingListCellRenderer());

        chatGUI.add(scrollPane);
        chatGUI.add(sendMessage);
        chatGUI.add(messageAreaScrollPane);
        chatGUI.add(backButton);
        chatGUI.add(separator);
        chatGUI.add(chatTitle);
        chatGUI.add(iconLabel);
        chatGUI.add(membersLabel);
        chatGUI.add(membersScrollPane);
        chatGUI.add(management);
        chatGUI.add(joinCall);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentChatUUID = null;
                switchContentPane(mainGUI);
            }
        });

        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message message = new Message(messageArea.getText(),username);
                JSONObject object = new JSONObject();
                object.put("header","send-message");
                object.put("data",message.toJson());
                object.put("chat",currentChatUUID);
                connector.sendObject(object.toString());
            }
        });
    }

    private void initCallGUI(){

    }

    private void initManageGUI(){

    }

    private void initFriendGUI(){

    }
    private void switchContentPane(Container c){
        setContentPane(c);
        repaint();
        revalidate();
    }

    private void updateChatNames(){
        int i = 0;
        String[] data = new String[directMessagesUUIDs.size()];
        for (String s : directMessagesUUIDs) {
            JSONObject info = chatInfoCache.getOrDefault(s,null);
            String name = s;
            if(info != null) name = info.getString("title");
            data[i] = name;
            i++;
        }
        directMessages.setListData(data);

        i = 0;
        data = new String[chatsUUIDs.size()];
        for (String s : chatsUUIDs) {
            JSONObject info = chatInfoCache.getOrDefault(s,null);
            String name = s;
            if(info != null) name = info.getString("title");
            data[i] = name;
            i++;
        }
        chats.setListData(data);

        i = 0;
        data = new String[friendsUsernames.size()];
        for (String s : friendsUsernames) {
            data[i] = s;
            i++;
        }
        friends.setListData(data);

        chatTitle.setText(chatInfoCache.getOrDefault(currentChatUUID,new JSONObject("{\"title\" : \"" + currentChatUUID + "\"}")).getString("title"));

        JSONObject info = chatInfoCache.getOrDefault(currentChatUUID,null);
        if(info != null){
            data = new String[info.getJSONArray("members").length()];
            for (int j = 0; j < info.getJSONArray("members").length(); j++) {
                data[j] = "<html><span style=\"font-size: 16px;\">" + info.getJSONArray("members").getString(j) + "</html>";
            }
            membersJList.setListData(data);
        }
    }

    private void updateData(){
        JSONObject packet = new JSONObject();
        packet.put("header","data-request");
        connector.sendObject(packet.toString());
    }
    private void startChatUpdater(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    for (String s : directMessagesUUIDs) {
                        JSONObject infoObject = new JSONObject();
                        infoObject.put("header", "chat-info");
                        infoObject.put("chat",s);
                        connector.sendObject(infoObject.toString());
                    }
                    for (String s : chatsUUIDs) {
                        JSONObject infoObject = new JSONObject();
                        infoObject.put("header", "chat-info");
                        infoObject.put("chat",s);
                        connector.sendObject(infoObject.toString());
                    }
                    updateChatNames();
                    if(currentChatUUID == null) continue;
                    JSONObject request = new JSONObject();
                    request.put("header","msg-update-check");
                    request.put("chat",currentChatUUID);
                    connector.sendObject(request.toString());
                }
            }
        });
        t.start();
    }
}
