package org.example.backendproject.stompwebsocket.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.stompwebsocket.dto.ChatMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msgBody = new String(message.getBody());
            ChatMessage chatMessage = objectMapper.readValue(msgBody, ChatMessage.class);
            simpMessagingTemplate.convertAndSend("/topic/room." + chatMessage.getRoomId(), chatMessage);
        } catch (JsonProcessingException e) {

        }
    }
}
