package com.example.websocketchatapplication.chat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.websocket.RemoteEndpoint;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebMvcTest
public class WebSocketChatServerTest {

    WebSocketChatServer chatServer;
    Session session;
    RemoteEndpoint.Basic endpoint;
    ArgumentCaptor<String> captor;
    List<Session> openSessions = new ArrayList();

    @Before
    public void setUp() {
        chatServer = new WebSocketChatServer();
        captor = ArgumentCaptor.forClass(String.class);
        endpoint = mock(RemoteEndpoint.Basic.class);
        session = createSession("username", endpoint);
    }

    @After
    public void shutDown() {
        openSessions.forEach(session -> chatServer.onClose(session));
    }

    private Session createSession(String username, Basic endpoint) {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(username);
        when(session.getBasicRemote()).thenReturn(endpoint);
        openSessions.add(session);
        return session;
    }

    private JSONObject sentObject() {
        return JSON.parseObject(captor.getValue());
    }

    @Test
    public void onOpen() throws IOException {
        chatServer.onOpen(session);
        verify(endpoint).sendText(captor.capture());
        assertEquals("ENTER", sentObject().getString("type"));
        assertEquals(1, sentObject().getIntValue("onlineCount"));
    }

    @Test
    public void onMessage() throws IOException {
        Map<String, String> message = new HashMap<>();
        message.put("username","username");
        message.put("message","message");
        chatServer.onOpen(session);
        chatServer.onMessage(session, JSON.toJSONString(message));

        verify(endpoint, times(2)).sendText(captor.capture());
        assertEquals("SPEAK", sentObject().getString("type"));
        assertEquals("username", sentObject().getString("username"));
        assertEquals("message", sentObject().getString("message"));
        assertEquals(1, sentObject().getIntValue("onlineCount"));
    }

    @Test
    public void onClose() throws IOException {
        Session anotherSession = createSession("another id", mock(Basic.class));
        chatServer.onOpen(session);
        chatServer.onOpen(anotherSession);
        chatServer.onClose(anotherSession);

        verify(endpoint, times(3)).sendText(captor.capture());
        assertEquals("QUIT", sentObject().getString("type"));
        assertEquals(1, sentObject().getIntValue("onlineCount"));
    }

}