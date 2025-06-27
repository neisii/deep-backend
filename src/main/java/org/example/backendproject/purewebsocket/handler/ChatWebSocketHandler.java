package org.example.backendproject.purewebsocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.example.backendproject.purewebsocket.dto.ChatMessage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    //세션을 관리하는 객체
    //Collections.synchronizedSet <- 여러 스레드가 동시에 이 객체에 접근할 때 동시설 문제를 안전하게 만들어주는 역할
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    private final ObjectMapper objectMapper = new ObjectMapper();

    //방과 방 안에 있는 세션을 관리하는 객체
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();


    // 클라이언트가 웹소켓 서버에 접속했을 때 호출
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        sessions.add(session);

        log.info("접속한 클라이언트 세션 아이디 = {}", session.getRemoteAddress());
}

    // 클라이언트가 보낸 메시지를 받았을 때 호출
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);

        String roomId = chatMessage.getRoomId();

        if (!rooms.containsKey(roomId)) {
            rooms.put(roomId, ConcurrentHashMap.newKeySet());

        }

        rooms.get(roomId).add(session);

        for (WebSocketSession s : sessions) {
            // for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));

                log.info("이름 = {}, 전송된 메시지 = {}", chatMessage.getFrom(), chatMessage.getMessage());
            }
            //}
        }
    }

    // 클라이언트의 연결이 끊어졌을 때 호출
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        sessions.remove(session);

        for (Set<WebSocketSession> room: rooms.values()) {
            room.remove(session);

        }
    }
}