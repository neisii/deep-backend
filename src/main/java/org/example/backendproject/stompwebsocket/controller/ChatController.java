package org.example.backendproject.stompwebsocket.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backendproject.stompwebsocket.dto.ChatMessage;
import org.example.backendproject.stompwebsocket.gpt.GptService;
import org.example.backendproject.stompwebsocket.redis.RedisPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    // 서버가 클라이언트에게 수동으로 메시지를 보낼 수 있도록 하는 클래스
    private final SimpMessagingTemplate template;

    @Value("${PROJECT_NAME:web Server}")
    private String instansName;

    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GptService gptService;

    // 단일 브로드캐스트용 (방 동적 생성 불가)
//    @MessageMapping({"chat.sendMessage"})
//    @SendTo("/topic/public")
//    public ChatMessage sendMessage(ChatMessage message) {
//        return message;
//    }

    // gpt 서비스로 전송
    @MessageMapping("/gpt")
    public void sendMessageGpt(ChatMessage message) throws Exception {

        // 내가 보낸 메시지 출력
        template.convertAndSend("/topic/gpt", message);

        String getResponse = gptService.gptMessage(message.getMessage());
        ChatMessage chatMessage = new ChatMessage("GPT says, ", getResponse);

        // Gtp 응답 출력
        template.convertAndSend("/topic/gpt", chatMessage);
    }


    // 동적 방 생성
    @MessageMapping({"chat.sendMessage"})
    public void sendmessage(ChatMessage message) throws JsonProcessingException {
        message.setMessage(message.getMessage());

        /*if (message.getTo() != null && !message.getTo().isEmpty()) {
            template.convertAndSendToUser(message.getTo(), "/queue/private", message);
        } else {
            template.convertAndSend("/topic/" + message.getRoomId(), message);
        }*/

        String channel;
        if (message.getTo() != null && !message.getTo().isEmpty()) {
            channel = "private" + message.getTo();
        } else {
            channel = "room." + message.getRoomId();
        }

        
        String msg = objectMapper.writeValueAsString(message);
        redisPublisher.publish(channel, msg);
    }

}
