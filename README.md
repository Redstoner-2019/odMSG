# <span style="color:green">Sending Messages</span>

header: "send-message"\
data: message as json\
chat: uuid of chat\

## result

no result

# <span style="color:green">request available chats

header: "request-chats"\

## result

header: "request-chats-result"\
direct-messages: json array of direct messages\
chats: json array of group chats\
friends: json array of friends\

# <span style="color:red">request user info

header: "data-request"\
data: username\

## result

header: "user-info"\
username: username\
displayname: displayname\

# <span style="color:green">request messages

header: "request-messages"\
chat: chat uuid\
message-start: start index from messages. (0 is newest message)\
message-end: end of messages to send, must be higher than message-start, if higher than amount of messages it will be clamped to the oldest message by the server\

## result

header: "request-messages"\
data: JSONArray of messages requested

# <span style="color:green">update check for messages

header: "msg-update-check"\
chat: chat uuid

## result

header: "msg-update-check"\
current-msg-id: current message id

# <span style="color:green">get chat info

header: "chat-info"
chat: chat uuid

## result

header: "chat-info"\
title: Titel\
members: members of the chat\
chat: uuid of chat\
last-msg: timestamp of last message