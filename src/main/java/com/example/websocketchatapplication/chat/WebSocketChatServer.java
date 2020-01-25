package com.example.websocketchatapplication.chat;

//import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server
 *
 * @see ServerEndpoint WebSocket Client
 * @see Session   WebSocket Session
 */

@Component
@ServerEndpoint("/chat")
public class WebSocketChatServer {
    /**
     * All chat sessions.
     */
    private static Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public static String jsonConverter(String type, String username, String message, int onlineCount) {

        String jsonStr = "";
        Message messageObj = new Message(type, username, message, onlineCount);
        // Creating Object of ObjectMapper define in Jakson Api
        ObjectMapper Obj = new ObjectMapper();
        // get Message object as a json string
        try {
            jsonStr = Obj.writeValueAsString(messageObj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    private static void sendMessageToAll(String message) {
        activeSessions.forEach((id, session) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * Open connection, 1) add session, 2) add user.
     */
    @OnOpen
    public void onOpen(Session session) {
        if(activeSessions.containsKey(session.getId())) {
            return;
        }
        activeSessions.put(session.getId(), session);
        sendMessageToAll(jsonConverter("ENTER", "", "", activeSessions.size()));
    }

    /**
     * Send message, 1) get username and session, 2) send message to all.
     */
    @OnMessage
    public void onMessage(Session session, String jsonString) {
        ObjectMapper Obj = new ObjectMapper();

        try {
            Message message = Obj.readValue(jsonString, Message.class);
            sendMessageToAll(jsonConverter("SPEAK", message.getUsername(), message.getMessage(), activeSessions.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Close connection, 1) remove session, 2) update user.
     */
    @OnClose
    public void onClose(Session session) {
        activeSessions.remove(session.getId());
        sendMessageToAll(jsonConverter("QUIT", "", "", activeSessions.size()));
    }

    /**
     * Print exception.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}
